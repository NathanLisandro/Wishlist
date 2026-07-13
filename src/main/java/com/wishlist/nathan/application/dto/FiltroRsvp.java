package com.wishlist.nathan.application.dto;

/**
 * Filtro de presença do painel de convidados. Espelha o que o badge mostra:
 * PENDENTES = sem RSVP; RECUSADOS = DECLINED ou CANCELLED ("Não vai").
 */
public enum FiltroRsvp {
    TODOS,
    CONFIRMADOS,
    PENDENTES,
    RECUSADOS
}
