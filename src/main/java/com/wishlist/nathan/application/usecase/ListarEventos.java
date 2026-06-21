package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.port.EventoRepositorioPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Lista os eventos da conta do admin. */
@Service
public class ListarEventos {

    private final EventoRepositorioPort eventos;

    public ListarEventos(EventoRepositorioPort eventos) {
        this.eventos = eventos;
    }

    @Transactional(readOnly = true)
    public List<EventoResumo> executar(UUID contaId) {
        return eventos.listarPorConta(contaId).stream()
                .map(e -> new EventoResumo(e.getId(), e.getNome(), e.getSlug().valor(),
                        e.getStatus().name(), e.getDataEvento(), e.getFotoUrl()))
                .toList();
    }

    public record EventoResumo(UUID id, String nome, String slug, String status,
                               LocalDate dataEvento, String fotoUrl) {}
}
