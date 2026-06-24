package com.wishlist.nathan.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades da aplicação (prefixo {@code app}). Segredos vêm de env vars.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Security security, Cors cors, MercadoPago mercadoPago, Storage storage, Ai ai) {

    public record Security(String piiEncryptionKey, String phoneHmacKey, String jwtSecret) {}

    public record Cors(String allowedOrigins) {}

    public record MercadoPago(String webhookBaseUrl) {}

    /**
     * Integração com a IA (Anthropic). {@code apiKey} vem de env var; {@code model}
     * é o ID do modelo (padrão claude-opus-4-8); {@code habilitado} liga/desliga a feature.
     */
    public record Ai(String apiKey, String model, boolean habilitado) {}

    /**
     * Armazenamento de fotos. {@code dir} = diretório físico (volume em Docker);
     * {@code publicBase} = prefixo da URL pública servida estaticamente (ex.: /media).
     */
    public record Storage(String dir, String publicBase) {}
}
