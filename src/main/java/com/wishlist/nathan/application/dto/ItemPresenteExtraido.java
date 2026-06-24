package com.wishlist.nathan.application.dto;

import java.math.BigDecimal;

/**
 * Item de presente extraído por IA de uma lista existente (foto/PDF/texto/link),
 * antes de ser materializado. {@code precoEstimado} (BRL) é opcional e preenchido
 * pela estimativa de preço. Mostrado em preview para o casal revisar/confirmar.
 */
public record ItemPresenteExtraido(String nome, String variante, Integer quantidade,
                                   String linkProduto, BigDecimal precoEstimado) {}
