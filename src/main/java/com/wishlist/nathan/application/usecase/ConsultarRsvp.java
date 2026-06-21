package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.port.RsvpRepositorioPort;
import com.wishlist.nathan.domain.rsvp.AcompanhanteRsvp;
import com.wishlist.nathan.domain.rsvp.Rsvp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Estado atual do RSVP do convidado (para a tela adaptar: já confirmou? marcou que
 * não vai? ainda não respondeu?). PENDING quando nunca respondeu.
 */
@Service
public class ConsultarRsvp {

    private final RsvpRepositorioPort rsvps;

    public ConsultarRsvp(RsvpRepositorioPort rsvps) {
        this.rsvps = rsvps;
    }

    @Transactional(readOnly = true)
    public Resultado executar(UUID eventoId, UUID convidadoId) {
        Rsvp rsvp = rsvps.buscarPorEventoEConvidado(eventoId, convidadoId).orElse(null);
        if (rsvp == null) {
            return new Resultado("PENDING", List.of());
        }
        List<String> acompanhantes = rsvp.getAcompanhantes().stream()
                .map(AcompanhanteRsvp::getNomeCompleto).toList();
        return new Resultado(rsvp.getStatus().name(), acompanhantes);
    }

    public record Resultado(String status, List<String> acompanhantes) {}
}
