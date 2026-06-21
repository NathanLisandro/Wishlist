package com.wishlist.nathan.infrastructure.persistence.adapter;

import com.wishlist.nathan.application.port.ConfigMercadoPagoRepositorioPort;
import com.wishlist.nathan.domain.comum.TokenCriptografado;
import com.wishlist.nathan.domain.conta.ConfigMercadoPago;
import com.wishlist.nathan.infrastructure.persistence.entity.ConfigMercadoPagoJpa;
import com.wishlist.nathan.infrastructure.persistence.repository.ConfigMercadoPagoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ConfigMercadoPagoRepositorioAdapter implements ConfigMercadoPagoRepositorioPort {

    private final ConfigMercadoPagoJpaRepository repo;

    public ConfigMercadoPagoRepositorioAdapter(ConfigMercadoPagoJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public ConfigMercadoPago salvarOuAtualizar(ConfigMercadoPago config) {
        ConfigMercadoPagoJpa jpa = repo.findByAccountId(config.getContaId())
                .orElseGet(ConfigMercadoPagoJpa::new);
        if (jpa.getId() == null) {
            jpa.setId(UUID.randomUUID());
        }
        jpa.setAccountId(config.getContaId());
        jpa.setAccessTokenEncrypted(config.getAccessToken().conteudo());
        jpa.setPublicKey(config.getPublicKey());
        jpa.setWebhookSecretEncrypted(
                config.getWebhookSecret() != null ? config.getWebhookSecret().conteudo() : null);
        return paraDominio(repo.save(jpa));
    }

    @Override
    public Optional<ConfigMercadoPago> buscarPorConta(UUID contaId) {
        return repo.findByAccountId(contaId).map(this::paraDominio);
    }

    private ConfigMercadoPago paraDominio(ConfigMercadoPagoJpa j) {
        TokenCriptografado webhook = j.getWebhookSecretEncrypted() != null
                ? new TokenCriptografado(j.getWebhookSecretEncrypted()) : null;
        return new ConfigMercadoPago(j.getId(), j.getAccountId(),
                new TokenCriptografado(j.getAccessTokenEncrypted()), j.getPublicKey(), webhook);
    }
}
