package com.wishlist.nathan.domain.presente;

/** Lançada ao tentar reservar um item já esgotado ou indisponível. */
public class PresenteEsgotadoException extends RuntimeException {
    public PresenteEsgotadoException(String mensagem) {
        super(mensagem);
    }
}
