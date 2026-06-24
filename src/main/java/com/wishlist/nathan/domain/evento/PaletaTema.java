package com.wishlist.nathan.domain.evento;

/**
 * Paletas pré-definidas para o site do casal. Whitelist garante que a combinação
 * de cores fique sempre legível/bonita; o casal só escolhe uma e, opcionalmente,
 * ajusta a cor de destaque. Persistida como texto (nome do enum).
 */
public enum PaletaTema {
    ROSE,      // padrão (rosé/plum/lilás atual)
    AZUL,
    VERDE,
    DOURADO,
    GRAFITE;

    /** Converte texto do banco/entrada para a paleta; valor inválido/nulo cai no padrão. */
    public static PaletaTema ofNullable(String valor) {
        if (valor == null || valor.isBlank()) {
            return ROSE;
        }
        try {
            return valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ROSE;
        }
    }
}
