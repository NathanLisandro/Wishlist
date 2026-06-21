package com.wishlist.nathan.application.excecao;

/** Recurso inexistente ou fora do escopo do tenant/sessão (mapeado para 404). */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
