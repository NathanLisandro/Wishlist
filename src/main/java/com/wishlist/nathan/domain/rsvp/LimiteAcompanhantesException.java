package com.wishlist.nathan.domain.rsvp;

/** Lançada quando a confirmação excede o limite de acompanhantes do convidado. */
public class LimiteAcompanhantesException extends RuntimeException {
    public LimiteAcompanhantesException(String mensagem) {
        super(mensagem);
    }
}
