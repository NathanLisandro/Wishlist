package com.wishlist.nathan.domain.pagamento;

import com.wishlist.nathan.domain.comum.Dinheiro;

import java.time.Instant;
import java.util.UUID;

/**
 * Presente em dinheiro via Mercado Pago. A confirmação só ocorre pelo webhook
 * server-side (nunca pelo redirect do cliente — ver SDD §6.3). A idempotência do
 * webhook é garantida pelo {@code mpPaymentId} único.
 */
public class PresenteDinheiro {

    private final UUID id;
    private final UUID eventoId;
    private final UUID convidadoId;     // opcional
    private final Dinheiro valor;
    private final String mensagem;
    private String mpPreferenceId;
    private String mpPaymentId;
    private StatusPresenteDinheiro status;
    private final Instant criadoEm;

    public PresenteDinheiro(UUID id, UUID eventoId, UUID convidadoId, Dinheiro valor, String mensagem,
                            String mpPreferenceId, String mpPaymentId, StatusPresenteDinheiro status,
                            Instant criadoEm) {
        if (eventoId == null) {
            throw new IllegalArgumentException("eventoId é obrigatório");
        }
        if (valor == null) {
            throw new IllegalArgumentException("valor é obrigatório");
        }
        this.id = id;
        this.eventoId = eventoId;
        this.convidadoId = convidadoId;
        this.valor = valor;
        this.mensagem = mensagem;
        this.mpPreferenceId = mpPreferenceId;
        this.mpPaymentId = mpPaymentId;
        this.status = status == null ? StatusPresenteDinheiro.PENDING : status;
        this.criadoEm = criadoEm;
    }

    /** Cria um presente em dinheiro pendente (antes de gerar a preference no MP). */
    public static PresenteDinheiro pendente(UUID eventoId, UUID convidadoId, Dinheiro valor, String mensagem) {
        return new PresenteDinheiro(null, eventoId, convidadoId, valor, mensagem,
                null, null, StatusPresenteDinheiro.PENDING, null);
    }

    public void vincularPreference(String mpPreferenceId) {
        this.mpPreferenceId = mpPreferenceId;
    }

    /** Aplica o resultado do pagamento vindo do webhook (idempotente por mpPaymentId). */
    public void aplicarPagamento(String mpPaymentId, StatusPresenteDinheiro novoStatus) {
        if (mpPaymentId == null || mpPaymentId.isBlank()) {
            throw new IllegalArgumentException("mpPaymentId é obrigatório");
        }
        this.mpPaymentId = mpPaymentId;
        this.status = novoStatus;
    }

    public boolean aprovado() {
        return status == StatusPresenteDinheiro.APPROVED;
    }

    public UUID getId() { return id; }
    public UUID getEventoId() { return eventoId; }
    public UUID getConvidadoId() { return convidadoId; }
    public Dinheiro getValor() { return valor; }
    public String getMensagem() { return mensagem; }
    public String getMpPreferenceId() { return mpPreferenceId; }
    public String getMpPaymentId() { return mpPaymentId; }
    public StatusPresenteDinheiro getStatus() { return status; }
    public Instant getCriadoEm() { return criadoEm; }
}
