package com.wishlist.nathan.domain.comum;

import java.util.regex.Pattern;

/**
 * Value object de slug usado na URL pública do evento (único por conta).
 */
public record Slug(String valor) {

    private static final Pattern PADRAO = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    public Slug {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Slug não pode ser vazio");
        }
        valor = valor.trim().toLowerCase();
        if (!PADRAO.matcher(valor).matches()) {
            throw new IllegalArgumentException("Slug inválido (use letras minúsculas, números e hifens)");
        }
    }
}
