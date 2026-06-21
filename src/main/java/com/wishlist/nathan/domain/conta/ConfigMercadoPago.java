package com.wishlist.nathan.domain.conta;

import com.wishlist.nathan.domain.comum.TokenCriptografado;

import java.util.UUID;

/**
 * Configuração do Mercado Pago por conta (1:1). Credenciais cifradas em repouso.
 */
public class ConfigMercadoPago {

    private final UUID id;
    private final UUID contaId;
    private TokenCriptografado accessToken;
    private String publicKey;
    private TokenCriptografado webhookSecret;

    public ConfigMercadoPago(UUID id, UUID contaId, TokenCriptografado accessToken,
                             String publicKey, TokenCriptografado webhookSecret) {
        if (contaId == null) {
            throw new IllegalArgumentException("contaId é obrigatório");
        }
        if (accessToken == null) {
            throw new IllegalArgumentException("accessToken é obrigatório");
        }
        this.id = id;
        this.contaId = contaId;
        this.accessToken = accessToken;
        this.publicKey = publicKey;
        this.webhookSecret = webhookSecret;
    }

    public void atualizarCredenciais(TokenCriptografado accessToken, String publicKey,
                                     TokenCriptografado webhookSecret) {
        if (accessToken == null) {
            throw new IllegalArgumentException("accessToken é obrigatório");
        }
        this.accessToken = accessToken;
        this.publicKey = publicKey;
        this.webhookSecret = webhookSecret;
    }

    public UUID getId() { return id; }
    public UUID getContaId() { return contaId; }
    public TokenCriptografado getAccessToken() { return accessToken; }
    public String getPublicKey() { return publicKey; }
    public TokenCriptografado getWebhookSecret() { return webhookSecret; }
}
