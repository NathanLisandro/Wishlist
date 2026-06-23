package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.port.ColaboradorEventoRepositorioPort;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Lista os eventos da conta: os próprios (dono) + os que ela co-gerencia. */
@Service
public class ListarEventos {

    private final EventoRepositorioPort eventos;
    private final ColaboradorEventoRepositorioPort colaboradores;

    public ListarEventos(EventoRepositorioPort eventos, ColaboradorEventoRepositorioPort colaboradores) {
        this.eventos = eventos;
        this.colaboradores = colaboradores;
    }

    @Transactional(readOnly = true)
    public List<EventoResumo> executar(UUID contaId) {
        List<EventoResumo> resultado = new ArrayList<>();
        for (Evento e : eventos.listarPorConta(contaId)) {
            resultado.add(resumo(e, true));
        }
        List<UUID> idsCompartilhados = colaboradores.listarEventoIdsDoColaborador(contaId);
        for (Evento e : eventos.listarPorIds(idsCompartilhados)) {
            resultado.add(resumo(e, false));
        }
        return resultado;
    }

    private EventoResumo resumo(Evento e, boolean dono) {
        return new EventoResumo(e.getId(), e.getNome(), e.getSlug().valor(), e.getStatus().name(),
                e.getDataEvento(), e.getFotoUrl(), dono);
    }

    public record EventoResumo(UUID id, String nome, String slug, String status,
                               LocalDate dataEvento, String fotoUrl, boolean dono) {}
}
