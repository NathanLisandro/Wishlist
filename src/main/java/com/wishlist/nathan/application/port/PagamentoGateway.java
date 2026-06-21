package com.wishlist.nathan.application.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Gateway de pagamento (Mercado Pago). As credenciais são por conta, então o
 * access token (decifrado) é passado em cada chamada.
 */
public interface PagamentoGateway {

    PreferenceCriada criarPreference(String accessToken, PreferenceInput input);

    PagamentoConsultado consultarPagamento(String accessToken, String paymentId);

    /** Valida a assinatura do webhook (header x-signature) contra o segredo da conta. */
    boolean validarAssinatura(String webhookSecret, String xSignature, String xRequestId, String dataId);

    record PreferenceInput(UUID moneyGiftId, BigDecimal valor, String descricao,
                           String notificationUrl, String backUrl) {}

    record PreferenceCriada(String preferenceId, String initPoint) {}

    /** status segue o vocabulário do MP: approved/pending/rejected/refunded. */
    record PagamentoConsultado(String paymentId, String status, String externalReference, BigDecimal valor) {}
}
