package com.wishlist.nathan.domain.evento;

/**
 * Estilo de animação de entrada dos elementos do site público. Whitelist; o CSS
 * correspondente (sob prefers-reduced-motion) vive no frontend. Persistido como texto.
 */
public enum EstiloAnimacao {
    NENHUMA,   // sem animação
    SUAVE,     // fade + slide-up (atual ".rise")
    CASCATA,   // entradas escalonadas
    FLUTUAR;   // leve flutuação contínua

    public static EstiloAnimacao ofNullable(String valor) {
        if (valor == null || valor.isBlank()) {
            return SUAVE;
        }
        try {
            return valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return SUAVE;
        }
    }
}
