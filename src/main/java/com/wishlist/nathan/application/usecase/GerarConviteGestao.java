package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * Gera (ou regenera) o token do link de co-gestão do evento. Só o DONO pode — e
 * regenerar invalida o link anterior. Devolve o token para o front montar a URL.
 */
@Service
public class GerarConviteGestao {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final EventoRepositorioPort eventos;

    public GerarConviteGestao(EventoRepositorioPort eventos) {
        this.eventos = eventos;
    }

    @Transactional
    public String executar(UUID contaId, UUID eventoId) {
        Evento evento = eventos.buscarPorId(eventoId)
                .filter(e -> e.getContaId().equals(contaId)) // só o dono convida
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        evento.definirTokenConviteGestao(token);
        eventos.salvar(evento);
        return token;
    }

    /** Remove o link de convite (revoga sem regenerar). */
    @Transactional
    public void revogar(UUID contaId, UUID eventoId) {
        Evento evento = eventos.buscarPorId(eventoId)
                .filter(e -> e.getContaId().equals(contaId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        evento.definirTokenConviteGestao(null);
        eventos.salvar(evento);
    }
}
