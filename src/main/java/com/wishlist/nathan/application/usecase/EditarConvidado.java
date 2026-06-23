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

/** Edita nome, telefone e nº de acompanhantes de um convidado (re-cifra e refaz o hash). */
@Service
public class EditarConvidado {

    private final AcessoEvento acessoEvento;
    private final ConvidadoRepositorioPort convidados;
    private final CriptografiaPort cripto;
    private final HmacPort hmac;

    public EditarConvidado(AcessoEvento acessoEvento, ConvidadoRepositorioPort convidados,
                           CriptografiaPort cripto, HmacPort hmac) {
        this.acessoEvento = acessoEvento;
        this.convidados = convidados;
        this.cripto = cripto;
        this.hmac = hmac;
    }

    @Transactional
    public void executar(UUID contaId, UUID eventoId, UUID convidadoId, Comando cmd) {
        Evento evento = acessoEvento.exigir(contaId, eventoId);
        Convidado atual = convidados.buscarPorId(convidadoId)
                .filter(c -> c.getEventoId().equals(evento.getId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Convidado não encontrado"));
        if (cmd.nomeCompleto() == null || cmd.nomeCompleto().isBlank()) {
            throw new IllegalArgumentException("Nome completo é obrigatório");
        }

        Telefone telefone = Telefone.deBrasil(cmd.telefone());
        Convidado atualizado = new Convidado(atual.getId(), evento.getId(),
                new NomeCriptografado(cripto.criptografar(cmd.nomeCompleto().trim())),
                new NomeCriptografado(cripto.criptografar(telefone.e164())),
                hmac.hash(telefone.e164()), (short) Math.max(0, cmd.acompanhantesPermitidos()),
                atual.getCriadoEm());
        try {
            convidados.salvar(atualizado);
        } catch (DataIntegrityViolationException e) {
            throw new ConflitoException("Já existe outro convidado com este telefone no evento");
        }
    }

    public record Comando(String nomeCompleto, String telefone, int acompanhantesPermitidos) {}
}
