package com.wishlist.nathan.application.excecao;

/** Falha de autenticação (login do admin) — mapeado para 401. */
public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
