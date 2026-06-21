package com.wishlist.nathan.application.port;

import com.wishlist.nathan.domain.importacao.StatusImportacao;
import com.wishlist.nathan.domain.importacao.TipoImportacao;

import java.util.UUID;

/** Persiste o resultado de uma importação (import_job), com relatório por linha. */
public interface ImportJobRepositorioPort {

    UUID registrar(Registro registro);

    record Registro(UUID eventoId, TipoImportacao tipo, String arquivo, StatusImportacao status,
                    int total, int ok, int falhas, String relatorioErrosJson) {}
}
