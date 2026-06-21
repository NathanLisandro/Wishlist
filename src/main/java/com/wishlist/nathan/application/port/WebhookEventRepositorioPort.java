package com.wishlist.nathan.application.port;

import java.util.UUID;

/** Idempotência/auditoria das notificações do Mercado Pago. */
public interface WebhookEventRepositorioPort {

    boolean existePorMpEventId(String mpEventId);

    void registrar(String mpEventId, UUID moneyGiftId, String payloadJson);
}
