package com.wishlist.nathan.application.excecao;

/** Conflito de estado (ex.: recurso duplicado, e-mail já cadastrado) — mapeado para 409. */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
