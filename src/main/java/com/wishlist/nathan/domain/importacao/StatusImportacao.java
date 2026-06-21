package com.wishlist.nathan.domain.importacao;

/** Status do job de importação. Espelha o enum {@code import_status} do banco. */
public enum StatusImportacao {
    PROCESSING,
    DONE,
    FAILED
}
