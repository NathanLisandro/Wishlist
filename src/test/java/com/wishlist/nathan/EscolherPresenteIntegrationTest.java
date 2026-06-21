package com.wishlist.nathan;

import com.wishlist.nathan.application.port.CategoriaEventoRepositorioPort;
import com.wishlist.nathan.application.usecase.AdicionarConvidado;
import com.wishlist.nathan.application.usecase.AdicionarPresente;
import com.wishlist.nathan.application.usecase.CriarEvento;
import com.wishlist.nathan.application.usecase.EscolherPresente;
import com.wishlist.nathan.application.usecase.RegistrarConta;
import com.wishlist.nathan.application.port.ReservaPresenteRepositorioPort;
import com.wishlist.nathan.domain.presente.PresenteEsgotadoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida o coração do sistema: reserva idempotente e sem oversell sob concorrência.
 */
@SpringBootTest
@Testcontainers
class EscolherPresenteIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void segredos(DynamicPropertyRegistry r) {
        r.add("app.security.pii-encryption-key", () -> Base64.getEncoder().encodeToString(new byte[32]));
        r.add("app.security.phone-hmac-key", () -> "chave-hmac-de-teste");
        r.add("app.security.jwt-secret", () -> "segredo-jwt-de-teste-bem-grande-1234567890");
    }

    @Autowired RegistrarConta registrarConta;
    @Autowired CriarEvento criarEvento;
    @Autowired AdicionarPresente adicionarPresente;
    @Autowired AdicionarConvidado adicionarConvidado;
    @Autowired EscolherPresente escolherPresente;
    @Autowired ReservaPresenteRepositorioPort reservas;
    @Autowired CategoriaEventoRepositorioPort categorias;

    @Test
    void reenvioComMesmaChaveNaoDuplica() {
        Contexto ctx = prepararCenario("idem", 1, 1);
        UUID convidado = ctx.convidados().get(0);
        String chave = UUID.randomUUID().toString();

        var r1 = escolherPresente.executar(new EscolherPresente.Comando(
                ctx.eventoId(), convidado, ctx.itemId(), chave, "127.0.0.1", "junit"));
        var r2 = escolherPresente.executar(new EscolherPresente.Comando(
                ctx.eventoId(), convidado, ctx.itemId(), chave, "127.0.0.1", "junit"));

        assertThat(r2.reservaId()).isEqualTo(r1.reservaId());
        assertThat(reservas.contarAtivasPorItem(ctx.itemId())).isEqualTo(1);
    }

    @Test
    void cliquesConcorrentesNaoCausamOversell() throws Exception {
        int convidadosN = 8;
        Contexto ctx = prepararCenario("concorrencia", 1 /* só 1 unidade */, convidadosN);

        ExecutorService pool = Executors.newFixedThreadPool(convidadosN);
        CountDownLatch largada = new CountDownLatch(1);
        AtomicInteger sucessos = new AtomicInteger();
        AtomicInteger esgotados = new AtomicInteger();

        for (UUID convidado : ctx.convidados()) {
            pool.submit(() -> {
                try {
                    largada.await();
                    escolherPresente.executar(new EscolherPresente.Comando(
                            ctx.eventoId(), convidado, ctx.itemId(),
                            UUID.randomUUID().toString(), "127.0.0.1", "junit"));
                    sucessos.incrementAndGet();
                } catch (PresenteEsgotadoException esgotado) {
                    esgotados.incrementAndGet();
                } catch (Exception ignorado) {
                    // demais falhas (ex.: lock timeout) não contam como sucesso
                }
                return null;
            });
        }
        largada.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)).isTrue();

        assertThat(sucessos.get()).isEqualTo(1);                 // exatamente 1 reserva
        assertThat(reservas.contarAtivasPorItem(ctx.itemId())).isEqualTo(1); // sem oversell
        assertThat(esgotados.get()).isEqualTo(convidadosN - 1);
    }

    private Contexto prepararCenario(String sufixo, int quantidade, int qtdConvidados) {
        // RegistrarConta retorna o contaId, usado direto pelos casos de uso de admin.
        UUID contaId = registrarConta.executar(new RegistrarConta.Comando(
                "casal-" + sufixo + "@teste.com", "senhaForte1", "Casal " + sufixo));
        short categoria = categorias.listarAtivas().get(0).getId();
        UUID eventoId = criarEvento.executar(contaId, new CriarEvento.Comando(
                categoria, "evento-" + sufixo, "Evento " + sufixo, null, null, true, null, true));
        UUID itemId = adicionarPresente.executar(contaId, eventoId, new AdicionarPresente.Comando(
                "Jogo de Panelas", "inox", quantidade, null, null));

        java.util.List<UUID> convidados = new java.util.ArrayList<>();
        for (int i = 0; i < qtdConvidados; i++) {
            UUID g = adicionarConvidado.executar(contaId, eventoId, new AdicionarConvidado.Comando(
                    "Convidado " + i, "+5511" + String.format("%09d", i + 100000000), 0));
            convidados.add(g);
        }
        return new Contexto(eventoId, itemId, convidados);
    }

    private record Contexto(UUID eventoId, UUID itemId, List<UUID> convidados) {}
}
