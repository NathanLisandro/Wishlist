package com.wishlist.nathan.domain.comum;

import java.util.Arrays;

/**
 * Envoltório de PII cifrada em repouso (nome/telefone do convidado).
 * Guarda apenas os bytes cifrados; a decifragem é responsabilidade da infraestrutura.
 */
public final class NomeCriptografado {

    private final byte[] conteudo;

    public NomeCriptografado(byte[] conteudo) {
        if (conteudo == null || conteudo.length == 0) {
            throw new IllegalArgumentException("Conteúdo cifrado não pode ser vazio");
        }
        this.conteudo = conteudo.clone();
    }

    public byte[] conteudo() {
        return conteudo.clone();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NomeCriptografado outro && Arrays.equals(conteudo, outro.conteudo);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(conteudo);
    }

    @Override
    public String toString() {
        return "NomeCriptografado[***]";
    }
}
