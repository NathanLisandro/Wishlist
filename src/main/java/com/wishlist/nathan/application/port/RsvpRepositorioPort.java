package com.wishlist.nathan.application.port;

import com.wishlist.nathan.domain.rsvp.Rsvp;
import com.wishlist.nathan.domain.rsvp.StatusRsvp;

import java.util.Optional;
import java.util.UUID;

public interface RsvpRepositorioPort {

    /** Salva o RSVP e seus acompanhantes (upsert por (event_id, guest_id)). */
    Rsvp salvar(Rsvp rsvp);

    Optional<Rsvp> buscarPorEventoEConvidado(UUID eventoId, UUID convidadoId);

    long contarPorEventoEStatus(UUID eventoId, StatusRsvp status);

    long contarAcompanhantesConfirmados(UUID eventoId);
}
