package com.wishlist.nathan.domain.comum;

/**
 * Value object de quantidade ofertada de um presente (sempre &gt; 0).
 */
public record Quantidade(int valor) {

    public Quantidade {
        if (valor <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
    }
}
