package com.wishlist.nathan.domain.comum;

/**
 * Value object da chave de idempotência (header {@code Idempotency-Key})
 * usada para garantir que reenvios de uma requisição não dupliquem efeitos.
 */
public record ChaveIdempotencia(String valor) {

    public ChaveIdempotencia {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Chave de idempotência não pode ser vazia");
        }
        valor = valor.trim();
    }
}
