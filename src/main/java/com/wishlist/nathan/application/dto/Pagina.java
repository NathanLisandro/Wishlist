package com.wishlist.nathan.application.dto;

import java.util.List;

/**
 * Página de resultados para listagens do admin. Mantém o tipo de paginação do
 * framework (Spring {@code Pageable}/{@code Page}) confinado aos adapters; a
 * aplicação e a apresentação trabalham só com este DTO.
 */
public record Pagina<T>(List<T> itens, long total, int page, int size) {

    /** Mapeia os itens preservando os metadados de paginação. */
    public <R> Pagina<R> mapear(java.util.function.Function<T, R> f) {
        return new Pagina<>(itens.stream().map(f).toList(), total, page, size);
    }
}
