package com.wishlist.nathan.domain.convidado;

/** Ações rastreadas no {@code guest_access_log}. Espelha o enum {@code guest_access_action}. */
public enum AcaoAcessoConvidado {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LIST_GIFTS,
    CLAIM_GIFT,
    CANCEL_CLAIM,
    MONEY_GIFT,
    RSVP_CONFIRM,
    RSVP_CANCEL
}
