package com.wishlist.nathan.domain.pagamento;

/** Status do presente em dinheiro. Espelha o enum {@code money_gift_status} do banco. */
public enum StatusPresenteDinheiro {
    PENDING,
    APPROVED,
    REJECTED,
    REFUNDED
}
