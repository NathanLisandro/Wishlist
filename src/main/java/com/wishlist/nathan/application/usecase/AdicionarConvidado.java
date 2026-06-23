package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.ConflitoException;
import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ConvidadoRepositorioPort;
import com.wishlist.nathan.application.port.CriptografiaPort;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.application.port.HmacPort;
import com.wishlist.nathan.domain.comum.NomeCriptografado;
import com.wishlist.nathan.domain.comum.Telefone;
import com.wishlist.nathan.domain.convidado.Convidado;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Adiciona um convidado (PII cifrada, phone_hash via HMAC) — alternativa direta ao import. */
@Service
public class AdicionarConvidado {

    private final AcessoEvento acessoEvento;
    private final ConvidadoRepositorioPort convidados;
    private final CriptografiaPort cripto;
    private final HmacPort hmac;

    public AdicionarConvidado(AcessoEvento acessoEvento, ConvidadoRepositorioPort convidados,
                              CriptografiaPort cripto, HmacPort hmac) {
        this.acessoEvento = acessoEvento;
        this.convidados = convidados;
        this.cripto = cripto;
        this.hmac = hmac;
    }

    @Transactional
    public UUID executar(UUID contaId, UUID eventoId, Comando cmd) {
        Evento evento = acessoEvento.exigir(contaId, eventoId);
        Telefone telefone = Telefone.deBrasil(cmd.telefone());
        byte[] phoneHash = hmac.hash(telefone.e164());
        if (cmd.nomeCompleto() == null || cmd.nomeCompleto().isBlank()) {
            throw new IllegalArgumentException("Nome completo é obrigatório");
        }
        Convidado convidado = new Convidado(null, evento.getId(),
                new NomeCriptografado(cripto.criptografar(cmd.nomeCompleto().trim())),
                new NomeCriptografado(cripto.criptografar(telefone.e164())),
                phoneHash, (short) Math.max(0, cmd.acompanhantesPermitidos()), null);
        try {
            return convidados.salvar(convidado).getId();
        } catch (DataIntegrityViolationException e) {
            throw new ConflitoException("Convidado com este telefone já existe no evento");
        }
    }

    public record Comando(String nomeCompleto, String telefone, int acompanhantesPermitidos) {}
}
