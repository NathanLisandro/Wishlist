package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.CredenciaisInvalidasException;
import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ConvidadoRepositorioPort;
import com.wishlist.nathan.application.port.CriptografiaPort;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.application.port.HmacPort;
import com.wishlist.nathan.application.port.RegistroAcessoPort;
import com.wishlist.nathan.application.port.TokenSessaoPort;
import com.wishlist.nathan.domain.comum.Telefone;
import com.wishlist.nathan.domain.convidado.AcaoAcessoConvidado;
import com.wishlist.nathan.domain.convidado.Convidado;
import com.wishlist.nathan.domain.evento.Evento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Identifica o convidado pelo telefone (lookup por HMAC) e emite token de sessão
 * escopado ao evento. Registra o acesso (sem telefone em claro).
 */
@Service
public class IniciarSessaoConvidado {

    private static final Logger log = LoggerFactory.getLogger("usecase");

    private final EventoRepositorioPort eventos;
    private final ConvidadoRepositorioPort convidados;
    private final HmacPort hmac;
    private final CriptografiaPort cripto;
    private final TokenSessaoPort tokens;
    private final RegistroAcessoPort acessos;

    public IniciarSessaoConvidado(EventoRepositorioPort eventos, ConvidadoRepositorioPort convidados,
                                  HmacPort hmac, CriptografiaPort cripto, TokenSessaoPort tokens,
                                  RegistroAcessoPort acessos) {
        this.eventos = eventos;
        this.convidados = convidados;
        this.hmac = hmac;
        this.cripto = cripto;
        this.tokens = tokens;
        this.acessos = acessos;
    }

    public Resultado executar(Comando cmd) {
        Evento evento = eventos.buscarPorSlug(cmd.slug())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        byte[] phoneHash = hmac.hash(Telefone.deBrasil(cmd.telefone()).e164());

        Optional<Convidado> achado = convidados.buscarPorEventoEPhoneHash(evento.getId(), phoneHash);
        if (achado.isEmpty()) {
            acessos.registrar(evento.getId(), null, phoneHash,
                    AcaoAcessoConvidado.LOGIN_FAILED, cmd.ip(), cmd.userAgent());
            log.warn("Login convidado FALHOU: telefone='{}' não está na lista do evento '{}'",
                    cmd.telefone(), cmd.slug());
            throw new CredenciaisInvalidasException("Telefone não encontrado na lista deste evento");
        }

        Convidado convidado = achado.get();
        acessos.registrar(evento.getId(), convidado.getId(), phoneHash,
                AcaoAcessoConvidado.LOGIN_SUCCESS, cmd.ip(), cmd.userAgent());

        String nome = cripto.descriptografar(convidado.getNomeCifrado().conteudo());
        log.info("Login convidado OK: nome='{}' telefone='{}' convidadoId={} evento='{}'",
                nome, cmd.telefone(), convidado.getId(), cmd.slug());
        String token = tokens.emitirTokenConvidado(evento.getId(), convidado.getId());
        return new Resultado(token, nome, convidado.getAcompanhantesPermitidos(),
                evento.getNome(), evento.getSlug().valor());
    }

    public record Comando(String slug, String telefone, String ip, String userAgent) {}

    public record Resultado(String token, String nomeConvidado, short acompanhantesPermitidos,
                            String nomeEvento, String slugEvento) {}
}
