package com.wishlist.nathan.domain.presente;

import com.wishlist.nathan.domain.comum.ChaveIdempotencia;

import java.time.Instant;
import java.util.UUID;

/**
 * Reserva de 1 unidade de um {@link ItemPresente} por um convidado.
 * A idempotência é garantida pela {@link ChaveIdempotencia} (única).
 */
public class ReservaPresente {

    private final UUID id;
    private final UUID itemPresenteId;
    private final UUID convidadoId;
    private StatusReserva status;
    private final ChaveIdempotencia chaveIdempotencia;
    private final Instant criadaEm;
    private Instant canceladaEm;

    public ReservaPresente(UUID id, UUID itemPresenteId, UUID convidadoId, StatusReserva status,
                           ChaveIdempotencia chaveIdempotencia, Instant criadaEm, Instant canceladaEm) {
        if (itemPresenteId == null || convidadoId == null) {
            throw new IllegalArgumentException("itemPresenteId e convidadoId são obrigatórios");
        }
        if (chaveIdempotencia == null) {
            throw new IllegalArgumentException("chaveIdempotencia é obrigatória");
        }
        this.id = id;
        this.itemPresenteId = itemPresenteId;
        this.convidadoId = convidadoId;
        this.status = status == null ? StatusReserva.CLAIMED : status;
        this.chaveIdempotencia = chaveIdempotencia;
        this.criadaEm = criadaEm;
        this.canceladaEm = canceladaEm;
    }

    public void cancelar(Instant agora) {
        if (status == StatusReserva.CANCELLED) {
            return;
        }
        this.status = StatusReserva.CANCELLED;
        this.canceladaEm = agora;
    }

    public boolean ativa() {
        return status == StatusReserva.CLAIMED;
    }

    public UUID getId() { return id; }
    public UUID getItemPresenteId() { return itemPresenteId; }
    public UUID getConvidadoId() { return convidadoId; }
    public StatusReserva getStatus() { return status; }
    public ChaveIdempotencia getChaveIdempotencia() { return chaveIdempotencia; }
    public Instant getCriadaEm() { return criadaEm; }
    public Instant getCanceladaEm() { return canceladaEm; }
}
