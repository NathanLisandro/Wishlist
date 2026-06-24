package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Liga/desliga o modo surpresa do evento. Apenas o DONO pode alternar (não o
 * co-gestor) — checagem direta de {@code contaId}, sem passar por {@link AcessoEvento}.
 */
@Service
public class AlternarModoSurpresa {

    private final EventoRepositorioPort eventos;

    public AlternarModoSurpresa(EventoRepositorioPort eventos) {
        this.eventos = eventos;
    }

    @Transactional
    public void executar(UUID contaId, UUID eventoId, boolean ativo) {
        Evento evento = eventos.buscarPorId(eventoId)
                .filter(e -> e.getContaId().equals(contaId)) // só o dono
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        evento.definirModoSurpresa(ativo);
        eventos.salvar(evento);
    }
}
