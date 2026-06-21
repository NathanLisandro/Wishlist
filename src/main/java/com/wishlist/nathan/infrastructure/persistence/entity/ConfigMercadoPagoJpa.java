package com.wishlist.nathan.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "mercado_pago_config")
@Getter
@Setter
@NoArgsConstructor
public class ConfigMercadoPagoJpa {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false, unique = true)
    private UUID accountId;

    @Column(name = "access_token_encrypted", nullable = false)
    private byte[] accessTokenEncrypted;

    @Column(name = "public_key")
    private String publicKey;

    @Column(name = "webhook_secret_encrypted")
    private byte[] webhookSecretEncrypted;
}
