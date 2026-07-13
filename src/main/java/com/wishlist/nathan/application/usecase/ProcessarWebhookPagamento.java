package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.CredenciaisInvalidasException;
import com.wishlist.nathan.application.port.ConfigMercadoPagoRepositorioPort;
import com.wishlist.nathan.application.port.CriptografiaPort;
import com.wishlist.nathan.application.port.MoneyGiftRepositorioPort;
import com.wishlist.nathan.application.port.PagamentoGateway;
import com.wishlist.nathan.application.port.WebhookEventRepositorioPort;
import com.wishlist.nathan.domain.conta.ConfigMercadoPago;
import com.wishlist.nathan.domain.pagamento.PresenteDinheiro;
import com.wishlist.nathan.domain.pagamento.StatusPresenteDinheiro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Processa a notificação do Mercado Pago. Idempotente: dedup por mp_event_id e
 * atualização por mp_payment_id (único). A confirmação só acontece aqui.
 */
@Service
public class ProcessarWebhookPagamento {

    private static final Logger log = LoggerFactory.getLogger(ProcessarWebhookPagamento.class);

    private final ConfigMercadoPagoRepositorioPort configs;
    private final MoneyGiftRepositorioPort moneyGifts;
    private final WebhookEventRepositorioPort webhookEvents;
    private final PagamentoGateway gateway;
    private final CriptografiaPort cripto;

    public ProcessarWebhookPagamento(ConfigMercadoPagoRepositorioPort configs, MoneyGiftRepositorioPort moneyGifts,
                                     WebhookEventRepositorioPort webhookEvents, PagamentoGateway gateway,
                                     CriptografiaPort cripto) {
        this.configs = configs;
        this.moneyGifts = moneyGifts;
        this.webhookEvents = webhookEvents;
        this.gateway = gateway;
        this.cripto = cripto;
    }

    @Transactional
    public void executar(Comando cmd) {
        // 1) Dedup de notificações repetidas.
        if (cmd.mpEventId() != null && webhookEvents.existePorMpEventId(cmd.mpEventId())) {
            log.info("Webhook {} já processado (dedup) — ignorando", cmd.mpEventId());
            return; // no-op
        }

        ConfigMercadoPago config = configs.buscarPorConta(cmd.contaId()).orElse(null);
        if (config == null) {
            log.warn("Webhook: conta {} sem config Mercado Pago — accountId errado na URL?", cmd.contaId());
            webhookEvents.registrar(cmd.mpEventId(), null, cmd.payloadBruto());
            return;
        }

        // 2) Validação de assinatura. Sem webhook secret não há como verificar a
        // origem da notificação — não confirmamos pagamento às cegas (poderia ser
        // forjada). Registramos o evento e saímos; o dono precisa configurar o secret.
        String webhookSecret = config.getWebhookSecret() != null
                ? cripto.descriptografar(config.getWebhookSecret().conteudo()) : null;
        if (webhookSecret == null) {
            log.warn("Webhook da conta {} sem webhook secret — assinatura não verificável; ignorando", cmd.contaId());
            webhookEvents.registrar(cmd.mpEventId(), null, cmd.payloadBruto());
            return;
        }
        if (!gateway.validarAssinatura(webhookSecret, cmd.xSignature(), cmd.xRequestId(), cmd.dataId())) {
            throw new CredenciaisInvalidasException("Assinatura do webhook inválida");
        }

        // 3) Só tratamos notificações de pagamento.
        if (!"payment".equalsIgnoreCase(cmd.tipo()) || cmd.dataId() == null) {
            log.info("Webhook ignorado (não é payment): type={} dataId={}", cmd.tipo(), cmd.dataId());
            webhookEvents.registrar(cmd.mpEventId(), null, cmd.payloadBruto());
            return;
        }

        String token = cripto.descriptografar(config.getAccessToken().conteudo());
        var pago = gateway.consultarPagamento(token, cmd.dataId());
        StatusPresenteDinheiro status = mapearStatus(pago.status());
        log.info("Pagamento {} consultado: statusMP={} -> {} (extRef={})",
                pago.paymentId(), pago.status(), status, pago.externalReference());

        UUID moneyGiftId = null;
        Optional<PresenteDinheiro> alvo = localizar(pago.externalReference(), pago.paymentId());
        if (alvo.isPresent()) {
            PresenteDinheiro presente = alvo.get();
            presente.aplicarPagamento(pago.paymentId(), status);
            moneyGiftId = moneyGifts.salvar(presente).getId();
            log.info("Money gift {} atualizado para {}", moneyGiftId, status);
        } else {
            log.warn("Pagamento {} sem money_gift correspondente (extRef={}) — nada atualizado",
                    pago.paymentId(), pago.externalReference());
        }

        webhookEvents.registrar(cmd.mpEventId(), moneyGiftId, cmd.payloadBruto());
    }

    private Optional<PresenteDinheiro> localizar(String externalReference, String paymentId) {
        if (externalReference != null) {
            try {
                Optional<PresenteDinheiro> p = moneyGifts.buscarPorId(UUID.fromString(externalReference));
                if (p.isPresent()) {
                    return p;
                }
            } catch (IllegalArgumentException ignored) {
                // external_reference não é UUID — tenta por payment id
            }
        }
        return moneyGifts.buscarPorMpPaymentId(paymentId);
    }

    private StatusPresenteDinheiro mapearStatus(String mp) {
        return switch (mp == null ? "" : mp.toLowerCase()) {
            case "approved" -> StatusPresenteDinheiro.APPROVED;
            case "rejected", "cancelled" -> StatusPresenteDinheiro.REJECTED;
            case "refunded", "charged_back" -> StatusPresenteDinheiro.REFUNDED;
            default -> StatusPresenteDinheiro.PENDING;
        };
    }

    public record Comando(UUID contaId, String tipo, String dataId, String mpEventId,
                          String xSignature, String xRequestId, String payloadBruto) {}
}
