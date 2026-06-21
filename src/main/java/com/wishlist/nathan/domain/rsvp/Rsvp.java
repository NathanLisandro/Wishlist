package com.wishlist.nathan.domain.rsvp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Confirmação de presença de um convidado (1 por convidado/evento). O número de
 * acompanhantes é derivado da lista {@link AcompanhanteRsvp} (não é coluna — 3NF).
 */
public class Rsvp {

    private final UUID id;
    private final UUID eventoId;
    private final UUID convidadoId;
    private StatusRsvp status;
    private final List<AcompanhanteRsvp> acompanhantes = new ArrayList<>();
    private Instant respondidoEm;

    public Rsvp(UUID id, UUID eventoId, UUID convidadoId, StatusRsvp status, Instant respondidoEm) {
        if (eventoId == null || convidadoId == null) {
            throw new IllegalArgumentException("eventoId e convidadoId são obrigatórios");
        }
        this.id = id;
        this.eventoId = eventoId;
        this.convidadoId = convidadoId;
        this.status = status;
        this.respondidoEm = respondidoEm;
    }

    /** Reconstrói um RSVP já persistido (com seus acompanhantes), sem revalidar limites. */
    public static Rsvp reconstituir(UUID id, UUID eventoId, UUID convidadoId, StatusRsvp status,
                                    Instant respondidoEm, List<String> nomesAcompanhantes) {
        Rsvp r = new Rsvp(id, eventoId, convidadoId, status, respondidoEm);
        if (nomesAcompanhantes != null) {
            for (String nome : nomesAcompanhantes) {
                r.acompanhantes.add(new AcompanhanteRsvp(null, id, nome, respondidoEm));
            }
        }
        return r;
    }

    /**
     * Confirma a presença com os nomes dos acompanhantes, validando o limite
     * permitido para o convidado. Reenvio sobrescreve a lista (upsert).
     *
     * @throws LimiteAcompanhantesException se a quantidade exceder o limite.
     */
    public void confirmar(List<String> nomesAcompanhantes, short limitePermitido, Instant agora) {
        List<String> nomes = nomesAcompanhantes == null ? List.of() : nomesAcompanhantes;
        if (nomes.size() > limitePermitido) {
            throw new LimiteAcompanhantesException(
                    "Limite de acompanhantes excedido: máximo " + limitePermitido);
        }
        this.status = StatusRsvp.CONFIRMED;
        this.respondidoEm = agora;
        this.acompanhantes.clear();
        for (String nome : nomes) {
            this.acompanhantes.add(new AcompanhanteRsvp(null, this.id, nome, agora));
        }
    }

    public void recusar(Instant agora) {
        this.status = StatusRsvp.DECLINED;
        this.respondidoEm = agora;
        this.acompanhantes.clear();
    }

    public void cancelar(Instant agora) {
        this.status = StatusRsvp.CANCELLED;
        this.respondidoEm = agora;
        this.acompanhantes.clear();
    }

    public int totalPessoas() {
        return 1 + acompanhantes.size();
    }

    public UUID getId() { return id; }
    public UUID getEventoId() { return eventoId; }
    public UUID getConvidadoId() { return convidadoId; }
    public StatusRsvp getStatus() { return status; }
    public List<AcompanhanteRsvp> getAcompanhantes() { return Collections.unmodifiableList(acompanhantes); }
    public Instant getRespondidoEm() { return respondidoEm; }
}
