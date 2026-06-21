package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.ConflitoException;
import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ConfigMercadoPagoRepositorioPort;
import com.wishlist.nathan.application.port.ConvidadoRepositorioPort;
import com.wishlist.nathan.application.port.CriptografiaPort;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.application.port.MoneyGiftRepositorioPort;
import com.wishlist.nathan.application.port.PagamentoGateway;
import com.wishlist.nathan.application.port.RegistroAcessoPort;
import com.wishlist.nathan.domain.comum.Dinheiro;
import com.wishlist.nathan.domain.conta.ConfigMercadoPago;
import com.wishlist.nathan.domain.convidado.AcaoAcessoConvidado;
import com.wishlist.nathan.domain.evento.Evento;
import com.wishlist.nathan.domain.pagamento.PresenteDinheiro;
import com.wishlist.nathan.infrastructure.config.AppProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Cria um presente em dinheiro (PENDING) e gera a preference no Mercado Pago.
 * A confirmação só ocorre pelo webhook (ProcessarWebhookPagamento).
 */
@Service
public class CriarPresenteDinheiro {

    private final EventoRepositorioPort eventos;
    private final ConvidadoRepositorioPort convidados;
    private final ConfigMercadoPagoRepositorioPort configs;
    private final MoneyGiftRepositorioPort moneyGifts;
    private final PagamentoGateway gateway;
    private final CriptografiaPort cripto;
    private final RegistroAcessoPort acessos;
    private final AppProperties props;

    public CriarPresenteDinheiro(EventoRepositorioPort eventos, ConvidadoRepositorioPort convidados,
                                 ConfigMercadoPagoRepositorioPort configs, MoneyGiftRepositorioPort moneyGifts,
                                 PagamentoGateway gateway, CriptografiaPort cripto,
                                 RegistroAcessoPort acessos, AppProperties props) {
        this.eventos = eventos;
        this.convidados = convidados;
        this.configs = configs;
        this.moneyGifts = moneyGifts;
        this.gateway = gateway;
        this.cripto = cripto;
        this.acessos = acessos;
        this.props = props;
    }

    @Transactional
    public Resultado executar(Comando cmd) {
        Evento evento = eventos.buscarPorId(cmd.eventoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        if (!evento.isPresenteDinheiroHabilitado()) {
            throw new ConflitoException("Presente em dinheiro desabilitado neste evento");
        }
        // Sessão antiga apontando para convidado removido/recriado: falha limpa (re-login)
        // em vez de violar a FK money_gift -> guest com um 500.
        convidados.buscarPorId(cmd.convidadoId())
                .filter(c -> c.getEventoId().equals(cmd.eventoId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sua sessão expirou. Entre novamente."));
        ConfigMercadoPago config = configs.buscarPorConta(evento.getContaId())
                .orElseThrow(() -> new ConflitoException("Mercado Pago não configurado para esta conta"));

        PresenteDinheiro presente = moneyGifts.salvar(PresenteDinheiro.pendente(
                cmd.eventoId(), cmd.convidadoId(), Dinheiro.reais(cmd.valor()), cmd.mensagem()));

        String token = cripto.descriptografar(config.getAccessToken().conteudo());
        String notificationUrl = props.mercadoPago().webhookBaseUrl()
                + "/webhooks/mercadopago?accountId=" + evento.getContaId();
        var pref = gateway.criarPreference(token, new PagamentoGateway.PreferenceInput(
                presente.getId(), cmd.valor(), "Presente para " + evento.getNome(),
                notificationUrl, baseFrontend()));

        presente.vincularPreference(pref.preferenceId());
        moneyGifts.salvar(presente);

        acessos.registrar(cmd.eventoId(), cmd.convidadoId(), null,
                AcaoAcessoConvidado.MONEY_GIFT, cmd.ip(), cmd.userAgent());
        return new Resultado(presente.getId(), pref.initPoint());
    }

    private String baseFrontend() {
        String origens = props.cors().allowedOrigins();
        return origens == null ? null : origens.split(",")[0].trim();
    }

    public record Comando(UUID eventoId, UUID convidadoId, BigDecimal valor, String mensagem,
                          String ip, String userAgent) {}

    public record Resultado(UUID moneyGiftId, String initPoint) {}
}
