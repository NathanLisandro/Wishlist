package com.wishlist.nathan.domain.presente;

import com.wishlist.nathan.domain.comum.ChaveIdempotencia;
import com.wishlist.nathan.domain.comum.Quantidade;

import java.time.Instant;
import java.util.UUID;

/**
 * Item de presente da lista. A quantidade reservada NÃO é armazenada (seria dado
 * derivado, violaria 3NF): é contada a partir das reservas ativas no momento da
 * escolha, sob lock de linha (ver SDD §6.1). Esgotamento é soft-delete: o item
 * vira {@code SOLD_OUT} e some da lista pública, nunca é apagado.
 */
public class ItemPresente {

    private final UUID id;
    private final UUID eventoId;
    private String nome;
    private String variante;        // cor / material / acabamento
    private Quantidade quantidadeTotal;
    private String linkProduto;
    private String imagemUrl;
    private StatusPresente status;
    private final String naturalKey; // hash p/ idempotência de import
    private final Instant criadoEm;

    public ItemPresente(UUID id, UUID eventoId, String nome, String variante, Quantidade quantidadeTotal,
                        String linkProduto, String imagemUrl, StatusPresente status,
                        String naturalKey, Instant criadoEm) {
        if (eventoId == null) {
            throw new IllegalArgumentException("eventoId é obrigatório");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome é obrigatório");
        }
        if (quantidadeTotal == null) {
            throw new IllegalArgumentException("quantidadeTotal é obrigatória");
        }
        if (naturalKey == null || naturalKey.isBlank()) {
            throw new IllegalArgumentException("naturalKey é obrigatória");
        }
        this.id = id;
        this.eventoId = eventoId;
        this.nome = nome;
        this.variante = variante;
        this.quantidadeTotal = quantidadeTotal;
        this.linkProduto = linkProduto;
        this.imagemUrl = imagemUrl;
        this.status = status == null ? StatusPresente.AVAILABLE : status;
        this.naturalKey = naturalKey;
        this.criadoEm = criadoEm;
    }

    /**
     * Reserva 1 unidade para o convidado, dado o número de reservas já ativas
     * (contado sob lock pela infraestrutura). Se atingir a capacidade, o item
     * passa a {@code SOLD_OUT}.
     *
     * @throws PresenteEsgotadoException se o item não estiver disponível ou esgotar.
     */
    public ReservaPresente reservar(UUID convidadoId, int reservasAtivas, ChaveIdempotencia chave) {
        if (status != StatusPresente.AVAILABLE) {
            throw new PresenteEsgotadoException("Item indisponível para reserva");
        }
        if (reservasAtivas >= quantidadeTotal.valor()) {
            throw new PresenteEsgotadoException("Item esgotado");
        }
        if (reservasAtivas + 1 == quantidadeTotal.valor()) {
            this.status = StatusPresente.SOLD_OUT;
        }
        return new ReservaPresente(null, this.id, convidadoId, StatusReserva.CLAIMED, chave, null, null);
    }

    /** Ao cancelar uma reserva, um item esgotado volta a ficar disponível. */
    public void liberarUnidade() {
        if (status == StatusPresente.SOLD_OUT) {
            this.status = StatusPresente.AVAILABLE;
        }
    }

    public void ocultar() {
        this.status = StatusPresente.HIDDEN;
    }

    public boolean disponivel() {
        return status == StatusPresente.AVAILABLE;
    }

    public UUID getId() { return id; }
    public UUID getEventoId() { return eventoId; }
    public String getNome() { return nome; }
    public String getVariante() { return variante; }
    public Quantidade getQuantidadeTotal() { return quantidadeTotal; }
    public String getLinkProduto() { return linkProduto; }
    public String getImagemUrl() { return imagemUrl; }
    public StatusPresente getStatus() { return status; }
    public String getNaturalKey() { return naturalKey; }
    public Instant getCriadoEm() { return criadoEm; }
}
