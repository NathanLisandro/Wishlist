package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ColaboradorEventoRepositorioPort;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Autorização de gestão do evento: a conta pode gerenciar se for a DONA
 * (event.account_id) OU uma co-gestora (event_collaborator). Centraliza a checagem
 * que antes era espalhada como {@code filter(e -> e.getContaId().equals(contaId))}.
 */
@Service
public class AcessoEvento {

    private final EventoRepositorioPort eventos;
    private final ColaboradorEventoRepositorioPort colaboradores;

    public AcessoEvento(EventoRepositorioPort eventos, ColaboradorEventoRepositorioPort colaboradores) {
        this.eventos = eventos;
        this.colaboradores = colaboradores;
    }

    /** Evento que a conta pode gerenciar (dono ou co-gestor). 404 se não tiver acesso. */
    public Evento exigir(UUID contaId, UUID eventoId) {
        Evento evento = eventos.buscarPorId(eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        if (podeGerenciar(contaId, evento)) {
            return evento;
        }
        throw new RecursoNaoEncontradoException("Evento não encontrado");
    }

    public boolean podeGerenciar(UUID contaId, Evento evento) {
        return evento.getContaId().equals(contaId)
                || colaboradores.existe(evento.getId(), contaId);
    }
}
