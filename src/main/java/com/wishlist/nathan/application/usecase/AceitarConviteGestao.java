package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ColaboradorEventoRepositorioPort;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * A conta autenticada aceita o convite (pelo token) e passa a co-gerenciar o evento.
 * Idempotente. Se a conta já for a dona, é no-op. Devolve o id do evento para redirecionar.
 */
@Service
public class AceitarConviteGestao {

    private final EventoRepositorioPort eventos;
    private final ColaboradorEventoRepositorioPort colaboradores;

    public AceitarConviteGestao(EventoRepositorioPort eventos,
                                ColaboradorEventoRepositorioPort colaboradores) {
        this.eventos = eventos;
        this.colaboradores = colaboradores;
    }

    @Transactional
    public UUID executar(UUID contaId, String token) {
        Evento evento = eventos.buscarPorTokenConviteGestao(token)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Convite inválido ou expirado"));
        if (!evento.getContaId().equals(contaId)) {
            colaboradores.adicionar(evento.getId(), contaId);
        }
        return evento.getId();
    }
}
