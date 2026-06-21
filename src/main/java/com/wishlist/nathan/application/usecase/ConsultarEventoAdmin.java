package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/** Detalhe completo do evento para o painel do admin (campos editáveis + foto/slug). */
@Service
public class ConsultarEventoAdmin {

    private final EventoRepositorioPort eventos;

    public ConsultarEventoAdmin(EventoRepositorioPort eventos) {
        this.eventos = eventos;
    }

    @Transactional(readOnly = true)
    public Resultado executar(UUID contaId, UUID eventoId) {
        Evento e = eventos.buscarPorId(eventoId)
                .filter(ev -> ev.getContaId().equals(contaId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        return new Resultado(e.getId(), e.getSlug().valor(), e.getNome(), e.getDescricao(),
                e.getDataEvento(), e.getLocal(), e.getMensagemCasal(), e.getMensagemFinal(),
                e.isRsvpHabilitado(), e.isPresenteDinheiroHabilitado(), e.getStatus().name(),
                e.getFotoUrl());
    }

    public record Resultado(UUID id, String slug, String nome, String descricao, LocalDate dataEvento,
                            String local, String mensagemCasal, String mensagemFinal,
                            boolean rsvpHabilitado, boolean presenteDinheiroHabilitado, String status,
                            String fotoUrl) {}
}
