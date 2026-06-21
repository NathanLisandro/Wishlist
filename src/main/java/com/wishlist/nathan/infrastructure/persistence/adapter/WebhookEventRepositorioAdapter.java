package com.wishlist.nathan.infrastructure.persistence.adapter;

import com.wishlist.nathan.application.port.WebhookEventRepositorioPort;
import com.wishlist.nathan.infrastructure.persistence.entity.PaymentWebhookEventJpa;
import com.wishlist.nathan.infrastructure.persistence.repository.PaymentWebhookEventJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class WebhookEventRepositorioAdapter implements WebhookEventRepositorioPort {

    private final PaymentWebhookEventJpaRepository repo;

    public WebhookEventRepositorioAdapter(PaymentWebhookEventJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean existePorMpEventId(String mpEventId) {
        return repo.existsByMpEventId(mpEventId);
    }

    @Override
    public void registrar(String mpEventId, UUID moneyGiftId, String payloadJson) {
        PaymentWebhookEventJpa jpa = new PaymentWebhookEventJpa();
        jpa.setId(UUID.randomUUID());
        jpa.setMpEventId(mpEventId);
        jpa.setMoneyGiftId(moneyGiftId);
        jpa.setPayload(payloadJson == null ? "{}" : payloadJson);
        jpa.setProcessedAt(Instant.now());
        repo.save(jpa);
    }
}
