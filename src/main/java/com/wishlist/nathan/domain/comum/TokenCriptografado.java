package com.wishlist.nathan.domain.comum;

import java.util.Arrays;

/**
 * Envoltório de credencial cifrada em repouso (ex.: access token do Mercado Pago).
 * Nunca deve ser logado nem exposto em claro.
 */
public final class TokenCriptografado {

    private final byte[] conteudo;

    public TokenCriptografado(byte[] conteudo) {
        if (conteudo == null || conteudo.length == 0) {
            throw new IllegalArgumentException("Token cifrado não pode ser vazio");
        }
        this.conteudo = conteudo.clone();
    }

    public byte[] conteudo() {
        return conteudo.clone();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TokenCriptografado outro && Arrays.equals(conteudo, outro.conteudo);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(conteudo);
    }

    @Override
    public String toString() {
        return "TokenCriptografado[***]";
    }
}
