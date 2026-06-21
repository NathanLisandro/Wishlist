package com.wishlist.nathan.infrastructure.payment;

import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.wishlist.nathan.application.port.PagamentoGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;

/**
 * Adapter do Mercado Pago (Checkout Pro). O access token é por conta, então cada
 * chamada usa {@link MPRequestOptions} com o token decifrado.
 */
@Component
public class MercadoPagoGateway implements PagamentoGateway {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoGateway.class);

    private final PreferenceClient preferenceClient = new PreferenceClient();
    private final PaymentClient paymentClient = new PaymentClient();

    @Override
    public PreferenceCriada criarPreference(String accessToken, PreferenceInput input) {
        try {
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(input.descricao())
                    .quantity(1)
                    .currencyId("BRL")
                    .unitPrice(input.valor())
                    .build();
            PreferenceRequest.PreferenceRequestBuilder builder = PreferenceRequest.builder()
                    .items(List.of(item))
                    .externalReference(input.moneyGiftId().toString())
                    .notificationUrl(input.notificationUrl());
            if (input.backUrl() != null && !input.backUrl().isBlank()) {
                builder.backUrls(PreferenceBackUrlsRequest.builder()
                        .success(input.backUrl()).pending(input.backUrl()).failure(input.backUrl()).build());
                builder.autoReturn("approved"); // redireciona automático quando aprovado (doc MP)
            }
            Preference pref = preferenceClient.create(builder.build(), opcoes(accessToken));
            return new PreferenceCriada(pref.getId(), pref.getInitPoint());
        } catch (MPApiException e) {
            // Surface o motivo REAL do Mercado Pago (ex.: back_url inválida, token errado).
            String detalhe = detalheMp(e);
            log.error("Mercado Pago recusou a preference (status {}): {}", statusMp(e), detalhe);
            throw new IllegalStateException("Mercado Pago recusou: " + detalhe, e);
        } catch (Exception e) {
            log.error("Falha ao criar preference no Mercado Pago", e);
            throw new IllegalStateException("Falha ao criar preference no Mercado Pago", e);
        }
    }

    private static int statusMp(MPApiException e) {
        return e.getApiResponse() != null ? e.getApiResponse().getStatusCode() : 0;
    }

    /** Corpo de erro do MP (JSON) — não contém PII do convidado, é seguro logar. */
    private static String detalheMp(MPApiException e) {
        if (e.getApiResponse() != null && e.getApiResponse().getContent() != null) {
            return e.getApiResponse().getContent();
        }
        return e.getMessage();
    }

    @Override
    public PagamentoConsultado consultarPagamento(String accessToken, String paymentId) {
        try {
            Payment pagamento = paymentClient.get(Long.parseLong(paymentId), opcoes(accessToken));
            return new PagamentoConsultado(
                    String.valueOf(pagamento.getId()),
                    pagamento.getStatus(),
                    pagamento.getExternalReference(),
                    pagamento.getTransactionAmount());
        } catch (MPApiException e) {
            String detalhe = detalheMp(e);
            log.error("Mercado Pago recusou a consulta do pagamento {} (status {}): {}",
                    paymentId, statusMp(e), detalhe);
            throw new IllegalStateException("Mercado Pago recusou a consulta: " + detalhe, e);
        } catch (Exception e) {
            log.error("Falha ao consultar pagamento {} no Mercado Pago", paymentId, e);
            throw new IllegalStateException("Falha ao consultar pagamento no Mercado Pago", e);
        }
    }

    /**
     * Valida o header x-signature ("ts=...,v1=...") contra o manifest
     * {@code id:<dataId>;request-id:<xRequestId>;ts:<ts>;} com HMAC-SHA256 do segredo.
     */
    @Override
    public boolean validarAssinatura(String webhookSecret, String xSignature, String xRequestId, String dataId) {
        if (webhookSecret == null || webhookSecret.isBlank() || xSignature == null) {
            return false;
        }
        String ts = null;
        String v1 = null;
        for (String parte : xSignature.split(",")) {
            String[] kv = parte.split("=", 2);
            if (kv.length == 2) {
                String chave = kv[0].trim();
                if (chave.equals("ts")) ts = kv[1].trim();
                else if (chave.equals("v1")) v1 = kv[1].trim();
            }
        }
        if (ts == null || v1 == null) {
            return false;
        }
        String manifest = "id:" + (dataId == null ? "" : dataId) + ";request-id:"
                + (xRequestId == null ? "" : xRequestId) + ";ts:" + ts + ";";
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String calculado = HexFormat.of().formatHex(mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8)));
            return constantesIguais(calculado, v1);
        } catch (Exception e) {
            return false;
        }
    }

    private MPRequestOptions opcoes(String accessToken) {
        return MPRequestOptions.builder().accessToken(accessToken).build();
    }

    private static boolean constantesIguais(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r |= a.charAt(i) ^ b.charAt(i);
        }
        return r == 0;
    }
}
