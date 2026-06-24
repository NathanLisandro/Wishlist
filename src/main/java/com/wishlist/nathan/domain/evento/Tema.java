package com.wishlist.nathan.domain.evento;

import java.util.regex.Pattern;

/**
 * Personalização visual escolhida pelo casal, como value object: paleta, cor de
 * destaque opcional (#RRGGBB), opacidade do overlay sobre o fundo (0..100) e estilo
 * de animação. A validação fica aqui (coesão), e o domínio só aplica um {@code Tema}
 * já válido. A imagem de fundo é tratada à parte (upload).
 */
public record Tema(PaletaTema paleta, String corDestaque, Integer overlay, EstiloAnimacao animacao) {

    private static final Pattern HEX = Pattern.compile("^#[0-9a-fA-F]{6}$");

    public Tema {
        paleta = paleta == null ? PaletaTema.ROSE : paleta;
        animacao = animacao == null ? EstiloAnimacao.SUAVE : animacao;
        if (corDestaque != null && corDestaque.isBlank()) {
            corDestaque = null;
        }
        if (corDestaque != null && !HEX.matcher(corDestaque).matches()) {
            throw new IllegalArgumentException("Cor de destaque inválida (use #RRGGBB)");
        }
        if (overlay != null && (overlay < 0 || overlay > 100)) {
            throw new IllegalArgumentException("Overlay deve estar entre 0 e 100");
        }
    }
}
