package com.wishlist.nathan.domain.comum;

import java.util.regex.Pattern;

/**
 * Value object de e-mail. Normaliza para minúsculas e valida formato.
 */
public record Email(String valor) {

    private static final Pattern PADRAO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("E-mail não pode ser vazio");
        }
        valor = valor.trim().toLowerCase();
        if (!PADRAO.matcher(valor).matches()) {
            throw new IllegalArgumentException("E-mail inválido");
        }
    }
}
