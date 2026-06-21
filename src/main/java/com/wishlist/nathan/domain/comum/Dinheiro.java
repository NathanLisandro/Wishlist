package com.wishlist.nathan.domain.comum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Value object monetário. Usa BigDecimal com 2 casas; padrão BRL.
 */
public record Dinheiro(BigDecimal valor, Currency moeda) {

    public static final Currency BRL = Currency.getInstance("BRL");

    public Dinheiro {
        if (valor == null) {
            throw new IllegalArgumentException("Valor não pode ser nulo");
        }
        if (moeda == null) {
            moeda = BRL;
        }
        valor = valor.setScale(2, RoundingMode.HALF_EVEN);
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException("Valor deve ser positivo");
        }
    }

    public static Dinheiro reais(BigDecimal valor) {
        return new Dinheiro(valor, BRL);
    }
}
