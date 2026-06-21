package com.wishlist.nathan.presentation;

import com.wishlist.nathan.application.usecase.ProcessarWebhookPagamento;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Webhook do Mercado Pago. A conta é identificada pelo query param accountId
 * (incluído na notification_url ao criar a preference).
 */
@RestController
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final ProcessarWebhookPagamento processar;
    private final ObjectMapper objectMapper;

    public WebhookController(ProcessarWebhookPagamento processar, ObjectMapper objectMapper) {
        this.processar = processar;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/webhooks/mercadopago")
    public ResponseEntity<Void> receber(
            @RequestParam("accountId") UUID accountId,
            @RequestParam(value = "type", required = false) String typeParam,
            @RequestParam(value = "topic", required = false) String topicParam,
            @RequestParam(value = "id", required = false) String idParam,
            @RequestParam(value = "data.id", required = false) String dataIdParam,
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestBody(required = false) String body) {

        String tipo = typeParam != null ? typeParam : topicParam;
        String dataId = dataIdParam;
        String eventId = idParam;

        // Corpo JSON tem prioridade sobre os query params.
        if (body != null && !body.isBlank()) {
            try {
                Map<?, ?> raiz = objectMapper.readValue(body, Map.class);
                if (raiz.get("type") != null) tipo = raiz.get("type").toString();
                if (raiz.get("id") != null) eventId = raiz.get("id").toString();
                if (raiz.get("data") instanceof Map<?, ?> data && data.get("id") != null) {
                    dataId = data.get("id").toString();
                }
            } catch (Exception ignorado) {
                // payload não-JSON: segue com os query params
            }
        }

        log.info("Webhook MP recebido: accountId={} type={} dataId={} eventId={}",
                accountId, tipo, dataId, eventId);
        processar.executar(new ProcessarWebhookPagamento.Comando(
                accountId, tipo, dataId, eventId, xSignature, xRequestId, body));
        return ResponseEntity.ok().build();
    }
}
