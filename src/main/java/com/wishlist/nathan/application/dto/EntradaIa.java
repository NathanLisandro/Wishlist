package com.wishlist.nathan.application.dto;

/**
 * Entrada para a extração por IA: uma imagem/print, um PDF/planilha, ou texto puro.
 * Links de outros sites são resolvidos para texto antes (via BuscadorHtmlPort) e
 * chegam aqui como {@link Tipo#TEXTO}.
 */
public record EntradaIa(Tipo tipo, byte[] binario, String contentType, String texto) {

    public enum Tipo { IMAGEM, PDF, TEXTO }

    public static EntradaIa imagem(byte[] binario, String contentType) {
        return new EntradaIa(Tipo.IMAGEM, binario, contentType, null);
    }

    public static EntradaIa pdf(byte[] binario) {
        return new EntradaIa(Tipo.PDF, binario, "application/pdf", null);
    }

    public static EntradaIa texto(String texto) {
        return new EntradaIa(Tipo.TEXTO, null, null, texto);
    }
}
