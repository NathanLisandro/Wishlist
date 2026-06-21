package com.wishlist.nathan.domain.comum;

import java.util.regex.Pattern;

/**
 * Value object de telefone em formato E.164 (ex.: +5511999998888).
 * Remove espaços, parênteses e hifens antes de validar.
 * O número em si é PII — nunca deve ser logado nem persistido em claro.
 */
public record Telefone(String e164) {

    private static final Pattern PADRAO = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    public Telefone {
        if (e164 == null) {
            throw new IllegalArgumentException("Telefone não pode ser nulo");
        }
        e164 = e164.trim().replaceAll("[\\s()\\-]", "");
        if (!PADRAO.matcher(e164).matches()) {
            throw new IllegalArgumentException("Telefone deve estar em formato E.164 (ex.: +5511999998888)");
        }
    }

    /**
     * Constrói a partir de uma entrada solta brasileira: aceita só DDD + número
     * (ex.: "67 99843-7332"), com ou sem o código do país, com ou sem máscara.
     * Quando não há código de país, assume Brasil (+55). Garante que o telefone
     * digitado pelo convidado e o cadastrado pelo casal gerem o MESMO hash.
     */
    public static Telefone deBrasil(String bruto) {
        if (bruto == null || bruto.isBlank()) {
            throw new IllegalArgumentException("Telefone é obrigatório");
        }
        String t = bruto.trim();
        if (t.startsWith("+")) {
            return new Telefone(t);
        }
        String dig = t.replaceAll("\\D", "");
        if (dig.isEmpty()) {
            throw new IllegalArgumentException("Telefone inválido");
        }
        if (dig.startsWith("55") && dig.length() >= 12) {
            return new Telefone("+" + dig);          // já tem código do país
        }
        if (dig.length() == 10 || dig.length() == 11) {
            return new Telefone("+55" + dig);        // DDD + número → assume Brasil
        }
        return new Telefone("+" + dig);              // deixa o regex validar o resto
    }

    /** Não expõe o número em logs/toString acidental. */
    @Override
    public String toString() {
        return "Telefone[***]";
    }
}
