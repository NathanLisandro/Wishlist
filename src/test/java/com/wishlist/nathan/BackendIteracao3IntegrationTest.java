package com.wishlist.nathan;

import com.wishlist.nathan.application.port.ConvidadoRepositorioPort;
import com.wishlist.nathan.application.port.MoneyGiftRepositorioPort;
import com.wishlist.nathan.application.port.PagamentoGateway;
import com.wishlist.nathan.application.usecase.AdicionarConvidado;
import com.wishlist.nathan.application.usecase.AdicionarPresente;
import com.wishlist.nathan.application.usecase.ConfigurarMercadoPago;
import com.wishlist.nathan.application.usecase.ConfirmarPresenca;
import com.wishlist.nathan.application.usecase.ConsultarDashboard;
import com.wishlist.nathan.application.usecase.CriarEvento;
import com.wishlist.nathan.application.usecase.CriarPresenteDinheiro;
import com.wishlist.nathan.application.usecase.EscolherPresente;
import com.wishlist.nathan.application.usecase.ImportarConvidados;
import com.wishlist.nathan.application.usecase.ImportarListaPresentes;
import com.wishlist.nathan.application.usecase.ProcessarWebhookPagamento;
import com.wishlist.nathan.application.usecase.RegistrarConta;
import com.wishlist.nathan.domain.pagamento.StatusPresenteDinheiro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cobre Mercado Pago (com stub do gateway), importação CSV e dashboard.
 */
@SpringBootTest
@Testcontainers
class BackendIteracao3IntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void segredos(DynamicPropertyRegistry r) {
        r.add("app.security.pii-encryption-key", () -> Base64.getEncoder().encodeToString(new byte[32]));
        r.add("app.security.phone-hmac-key", () -> "chave-hmac-de-teste");
        r.add("app.security.jwt-secret", () -> "segredo-jwt-de-teste-bem-grande-1234567890");
    }

    /** Stub do gateway de pagamento (sem chamadas reais ao Mercado Pago). */
    @TestConfiguration
    static class StubConfig {
        @Bean
        @Primary
        StubPagamentoGateway stubGateway() {
            return new StubPagamentoGateway();
        }
    }

    static class StubPagamentoGateway implements PagamentoGateway {
        final AtomicInteger consultas = new AtomicInteger();
        volatile String externalReference;
        volatile String status = "approved";

        @Override
        public PreferenceCriada criarPreference(String accessToken, PreferenceInput input) {
            externalReference = input.moneyGiftId().toString();
            return new PreferenceCriada("pref-123", "https://mp/init/pref-123");
        }

        @Override
        public PagamentoConsultado consultarPagamento(String accessToken, String paymentId) {
            consultas.incrementAndGet();
            return new PagamentoConsultado(paymentId, status, externalReference, BigDecimal.TEN);
        }

        @Override
        public boolean validarAssinatura(String webhookSecret, String xSignature, String xRequestId, String dataId) {
            return true;
        }
    }

    @Autowired RegistrarConta registrarConta;
    @Autowired CriarEvento criarEvento;
    @Autowired AdicionarConvidado adicionarConvidado;
    @Autowired AdicionarPresente adicionarPresente;
    @Autowired ConfigurarMercadoPago configurarMercadoPago;
    @Autowired CriarPresenteDinheiro criarPresenteDinheiro;
    @Autowired ProcessarWebhookPagamento processarWebhook;
    @Autowired ImportarConvidados importarConvidados;
    @Autowired ImportarListaPresentes importarPresentes;
    @Autowired ConsultarDashboard consultarDashboard;
    @Autowired ConfirmarPresenca confirmarPresenca;
    @Autowired EscolherPresente escolherPresente;
    @Autowired com.wishlist.nathan.application.usecase.ConsultarEventoPublico consultarEventoPublico;
    @Autowired MoneyGiftRepositorioPort moneyGifts;
    @Autowired ConvidadoRepositorioPort convidados;
    @Autowired StubPagamentoGateway stub;

    @Test
    void mercadoPagoFluxoEDedupDoWebhook() {
        UUID conta = registrarConta.executar(new RegistrarConta.Comando("mp@teste.com", "senhaForte1", "MP"));
        configurarMercadoPago.executar(conta, new ConfigurarMercadoPago.Comando("TEST-token", "PUB", "segredo-wh"));
        UUID evento = criarEvento.executar(conta, new CriarEvento.Comando(
                cat(), "evento-mp", "Evento MP", null, null, true, null, true));
        UUID convidado = adicionarConvidado.executar(conta, evento, new AdicionarConvidado.Comando(
                "Doador", "+5511900000001", 0));

        var r = criarPresenteDinheiro.executar(new CriarPresenteDinheiro.Comando(
                evento, convidado, new BigDecimal("150.00"), "Parabéns!", "127.0.0.1", "junit"));
        assertThat(r.initPoint()).isEqualTo("https://mp/init/pref-123");
        assertThat(moneyGifts.buscarPorId(r.moneyGiftId()).orElseThrow().getStatus())
                .isEqualTo(StatusPresenteDinheiro.PENDING);

        // Webhook aprova o pagamento.
        stub.externalReference = r.moneyGiftId().toString();
        processarWebhook.executar(new ProcessarWebhookPagamento.Comando(
                conta, "payment", "PAY-1", "EVT-1", "ts=1,v1=x", "req-1", "{}"));
        assertThat(moneyGifts.buscarPorId(r.moneyGiftId()).orElseThrow().getStatus())
                .isEqualTo(StatusPresenteDinheiro.APPROVED);

        int consultasApos1 = stub.consultas.get();
        // Reenvio do MESMO mp_event_id é no-op (dedup) — não consulta o MP de novo.
        processarWebhook.executar(new ProcessarWebhookPagamento.Comando(
                conta, "payment", "PAY-1", "EVT-1", "ts=1,v1=x", "req-1", "{}"));
        assertThat(stub.consultas.get()).isEqualTo(consultasApos1);
    }

    @Test
    void importacaoConvidadosIdempotente() {
        UUID conta = registrarConta.executar(new RegistrarConta.Comando("imp@teste.com", "senhaForte1", "Imp"));
        UUID evento = criarEvento.executar(conta, new CriarEvento.Comando(
                cat(), "evento-imp", "Evento Imp", null, null, true, null, true));

        String csv = "nome_completo,telefone,acompanhantes\n"
                + "Carlos Silva,+5511900000010,2\n"
                + "Maria Souza,+5511900000011,0\n"
                + "Invalido,abc,0\n";
        var r1 = importarConvidados.executar(conta, evento, csv.getBytes(StandardCharsets.UTF_8), "c.csv");
        assertThat(r1.total()).isEqualTo(3);
        assertThat(r1.ok()).isEqualTo(2);
        assertThat(r1.falhas()).isEqualTo(1);
        assertThat(convidados.contarPorEvento(evento)).isEqualTo(2);

        // Re-subir não duplica (dedup por phone_hash).
        var r2 = importarConvidados.executar(conta, evento, csv.getBytes(StandardCharsets.UTF_8), "c.csv");
        assertThat(r2.ok()).isZero();
        assertThat(convidados.contarPorEvento(evento)).isEqualTo(2);
    }

    @Test
    void importacaoPresentes() {
        UUID conta = registrarConta.executar(new RegistrarConta.Comando("impg@teste.com", "senhaForte1", "ImpG"));
        UUID evento = criarEvento.executar(conta, new CriarEvento.Comando(
                cat(), "evento-impg", "Evento ImpG", null, null, true, null, true));
        String csv = "nome,quantidade,variante,link,imagem_url\n"
                + "Jogo de Panelas,2,inox,,\n"
                + "Liquidificador,1,,,\n";
        var r = importarPresentes.executar(conta, evento, csv.getBytes(StandardCharsets.UTF_8), "p.csv");
        assertThat(r.total()).isEqualTo(2);
        assertThat(r.ok()).isEqualTo(2);
    }

    @Test
    void dashboardAgrega() {
        UUID conta = registrarConta.executar(new RegistrarConta.Comando("dash@teste.com", "senhaForte1", "Dash"));
        configurarMercadoPago.executar(conta, new ConfigurarMercadoPago.Comando("TEST-token", "PUB", "wh"));
        UUID evento = criarEvento.executar(conta, new CriarEvento.Comando(
                cat(), "evento-dash", "Evento Dash", null, null, true, null, true));
        UUID g1 = adicionarConvidado.executar(conta, evento, new AdicionarConvidado.Comando("Ana", "+5511900000020", 2));
        adicionarConvidado.executar(conta, evento, new AdicionarConvidado.Comando("Beto", "+5511900000021", 0));
        UUID presente = adicionarPresente.executar(conta, evento, new AdicionarPresente.Comando(
                "Panela", "inox", 3, null, null));

        confirmarPresenca.executar(new ConfirmarPresenca.Comando(
                evento, g1, List.of("Acomp 1"), "127.0.0.1", "junit"));
        escolherPresente.executar(new EscolherPresente.Comando(
                evento, g1, presente, UUID.randomUUID().toString(), "127.0.0.1", "junit"));

        var mg = criarPresenteDinheiro.executar(new CriarPresenteDinheiro.Comando(
                evento, g1, new BigDecimal("10.00"), null, "127.0.0.1", "junit"));
        stub.externalReference = mg.moneyGiftId().toString();
        processarWebhook.executar(new ProcessarWebhookPagamento.Comando(
                conta, "payment", "PAY-D", "EVT-D", "ts=1,v1=x", "req", "{}"));

        var d = consultarDashboard.executar(conta, evento);
        assertThat(d.convidados()).isEqualTo(2);
        assertThat(d.confirmados()).isEqualTo(1);
        assertThat(d.acompanhantesConfirmados()).isEqualTo(1);
        assertThat(d.totalPessoasConfirmadas()).isEqualTo(2);
        assertThat(d.presentesReservados()).isEqualTo(1);
        assertThat(d.presentesDinheiroAprovados()).isEqualTo(1);
        assertThat(d.dinheiroRecebido()).isEqualByComparingTo("10.00");
        assertThat(d.acessosRecentes()).isNotEmpty();
    }

    @Test
    void eventoPublicoPorSlug() {
        UUID conta = registrarConta.executar(new RegistrarConta.Comando("pub@teste.com", "senhaForte1", "Pub"));
        criarEvento.executar(conta, new CriarEvento.Comando(
                cat(), "ana-e-beto", "Casamento Ana & Beto", "Nosso grande dia", null, true, null, true));
        var r = consultarEventoPublico.executar("ana-e-beto");
        assertThat(r.nome()).isEqualTo("Casamento Ana & Beto");
        assertThat(r.categoria()).isEqualTo("Casamento");
        assertThat(r.presenteDinheiroHabilitado()).isTrue();
    }

    private short cat() {
        return (short) 1; // CASAMENTO (seed V1)
    }
}
