package com.wishlist.nathan.domain.evento;

/**
 * Categoria do evento (tabela de domínio {@code event_category}).
 * Extensível sem migration de schema; {@code OUTRO} cobre casos fora da lista.
 */
public class CategoriaEvento {

    private final short id;
    private final String code;
    private final String label;
    private final boolean ativa;

    public CategoriaEvento(short id, String code, String label, boolean ativa) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code é obrigatório");
        }
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("label é obrigatório");
        }
        this.id = id;
        this.code = code;
        this.label = label;
        this.ativa = ativa;
    }

    public short getId() { return id; }
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public boolean isAtiva() { return ativa; }
}
