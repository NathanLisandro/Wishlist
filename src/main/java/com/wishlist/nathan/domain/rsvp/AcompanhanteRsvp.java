package com.wishlist.nathan.domain.rsvp;

import java.time.Instant;
import java.util.UUID;

/** Acompanhante confirmado dentro de um RSVP. */
public class AcompanhanteRsvp {

    private final UUID id;
    private final UUID rsvpId;
    private final String nomeCompleto;
    private final Instant criadoEm;

    public AcompanhanteRsvp(UUID id, UUID rsvpId, String nomeCompleto, Instant criadoEm) {
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            throw new IllegalArgumentException("nomeCompleto do acompanhante é obrigatório");
        }
        this.id = id;
        this.rsvpId = rsvpId;
        this.nomeCompleto = nomeCompleto.trim();
        this.criadoEm = criadoEm;
    }

    public UUID getId() { return id; }
    public UUID getRsvpId() { return rsvpId; }
    public String getNomeCompleto() { return nomeCompleto; }
    public Instant getCriadoEm() { return criadoEm; }
}
