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
        // mp_event_id é NOT NULL/UNIQUE; algumas notificações vêm sem id.
        jpa.setMpEventId(mpEventId != null && !mpEventId.isBlank()
                ? mpEventId : UUID.randomUUID().toString());
        jpa.setMoneyGiftId(moneyGiftId);
        // Coluna jsonb: IPNs do MP (merchant_order/payment via query-param) chegam com
        // corpo VAZIO. Gravar "" quebra o jsonb — guardamos só JSON válido, senão "{}".
        jpa.setPayload(jsonValido(payloadJson));
        jpa.setProcessedAt(Instant.now());
        repo.save(jpa);
    }

    private static String jsonValido(String payload) {
        if (payload == null) {
            return "{}";
        }
        String p = payload.trim();
        return p.startsWith("{") || p.startsWith("[") ? p : "{}";
    }
}
