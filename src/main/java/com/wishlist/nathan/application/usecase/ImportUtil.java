package com.wishlist.nathan.application.usecase;

/** Utilitários de parsing para importação de planilhas. */
final class ImportUtil {

    private ImportUtil() {
    }

    static String vazioParaNull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    static int inteiroObrigatorio(String v, String campo) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException("Coluna '" + campo + "' é obrigatória");
        }
        String limpo = v.trim();
        if (limpo.endsWith(".0")) { // XLSX costuma trazer numéricos como "5.0"
            limpo = limpo.substring(0, limpo.length() - 2);
        }
        try {
            return Integer.parseInt(limpo);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Coluna '" + campo + "' deve ser um número inteiro");
        }
    }

    static int inteiroOpcional(String v) {
        if (v == null || v.isBlank()) {
            return 0;
        }
        String limpo = v.trim();
        if (limpo.endsWith(".0")) {
            limpo = limpo.substring(0, limpo.length() - 2);
        }
        try {
            return Integer.parseInt(limpo);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Coluna 'acompanhantes' deve ser um número inteiro");
        }
    }
}
