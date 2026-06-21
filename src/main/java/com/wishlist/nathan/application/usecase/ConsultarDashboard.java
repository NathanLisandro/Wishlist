package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ConvidadoRepositorioPort;
import com.wishlist.nathan.application.port.CriptografiaPort;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.application.port.MoneyGiftRepositorioPort;
import com.wishlist.nathan.application.port.RegistroAcessoPort;
import com.wishlist.nathan.application.port.ReservaPresenteRepositorioPort;
import com.wishlist.nathan.application.port.RsvpRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import com.wishlist.nathan.domain.rsvp.StatusRsvp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Painel do admin: presenças, presentes e dinheiro recebido, além dos acessos
 * recentes. O nome do convidado é decifrado SÓ aqui (para o painel autorizado).
 */
@Service
public class ConsultarDashboard {

    private final EventoRepositorioPort eventos;
    private final ConvidadoRepositorioPort convidados;
    private final RsvpRepositorioPort rsvps;
    private final ReservaPresenteRepositorioPort reservas;
    private final MoneyGiftRepositorioPort moneyGifts;
    private final RegistroAcessoPort acessos;
    private final CriptografiaPort cripto;

    public ConsultarDashboard(EventoRepositorioPort eventos, ConvidadoRepositorioPort convidados,
                              RsvpRepositorioPort rsvps, ReservaPresenteRepositorioPort reservas,
                              MoneyGiftRepositorioPort moneyGifts, RegistroAcessoPort acessos,
                              CriptografiaPort cripto) {
        this.eventos = eventos;
        this.convidados = convidados;
        this.rsvps = rsvps;
        this.reservas = reservas;
        this.moneyGifts = moneyGifts;
        this.acessos = acessos;
        this.cripto = cripto;
    }

    @Transactional(readOnly = true)
    public Resultado executar(UUID contaId, UUID eventoId) {
        Evento evento = eventos.buscarPorId(eventoId)
                .filter(e -> e.getContaId().equals(contaId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));

        long confirmados = rsvps.contarPorEventoEStatus(eventoId, StatusRsvp.CONFIRMED);
        long acompanhantes = rsvps.contarAcompanhantesConfirmados(eventoId);
        // "Não vão poder ir": o botão grava CANCELLED; contamos também DECLINED por robustez.
        long recusados = rsvps.contarPorEventoEStatus(eventoId, StatusRsvp.DECLINED)
                + rsvps.contarPorEventoEStatus(eventoId, StatusRsvp.CANCELLED);

        Map<UUID, String> nomes = new HashMap<>();
        List<Acesso> acessosRecentes = acessos.listarRecentes(eventoId).stream()
                .map(a -> new Acesso(a.acao().name(), nomeDe(a.convidadoId(), nomes), a.ip(), a.quando()))
                .toList();

        return new Resultado(
                evento.getNome(),
                convidados.contarPorEvento(eventoId),
                confirmados,
                recusados,
                acompanhantes,
                confirmados + acompanhantes,
                reservas.contarAtivasPorEvento(eventoId),
                moneyGifts.contarAprovadosPorEvento(eventoId),
                moneyGifts.somarAprovadosPorEvento(eventoId),
                acessosRecentes);
    }

    private String nomeDe(UUID convidadoId, Map<UUID, String> cache) {
        if (convidadoId == null) {
            return null;
        }
        return cache.computeIfAbsent(convidadoId, id ->
                convidados.buscarPorId(id)
                        .map(c -> cripto.descriptografar(c.getNomeCifrado().conteudo()))
                        .orElse(null));
    }

    public record Resultado(String nomeEvento, long convidados, long confirmados, long recusados,
                            long acompanhantesConfirmados, long totalPessoasConfirmadas,
                            long presentesReservados, long presentesDinheiroAprovados,
                            BigDecimal dinheiroRecebido, List<Acesso> acessosRecentes) {}

    public record Acesso(String acao, String nomeConvidado, String ip, Instant quando) {}
}
