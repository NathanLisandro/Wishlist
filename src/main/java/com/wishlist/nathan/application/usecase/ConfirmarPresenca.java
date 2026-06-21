package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.ConflitoException;
import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ConvidadoRepositorioPort;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.application.port.RegistroAcessoPort;
import com.wishlist.nathan.application.port.RsvpRepositorioPort;
import com.wishlist.nathan.domain.convidado.AcaoAcessoConvidado;
import com.wishlist.nathan.domain.convidado.Convidado;
import com.wishlist.nathan.domain.evento.Evento;
import com.wishlist.nathan.domain.rsvp.Rsvp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Confirma presença do convidado + acompanhantes (até o limite). Reenvio atualiza (upsert). */
@Service
public class ConfirmarPresenca {

    private final EventoRepositorioPort eventos;
    private final ConvidadoRepositorioPort convidados;
    private final RsvpRepositorioPort rsvps;
    private final RegistroAcessoPort acessos;

    public ConfirmarPresenca(EventoRepositorioPort eventos, ConvidadoRepositorioPort convidados,
                             RsvpRepositorioPort rsvps, RegistroAcessoPort acessos) {
        this.eventos = eventos;
        this.convidados = convidados;
        this.rsvps = rsvps;
        this.acessos = acessos;
    }

    @Transactional
    public int executar(Comando cmd) {
        Instant agora = Instant.now();
        Evento evento = eventos.buscarPorId(cmd.eventoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        if (!evento.rsvpAberto(agora)) {
            throw new ConflitoException("RSVP indisponível ou fora do prazo");
        }
        Convidado convidado = convidados.buscarPorId(cmd.convidadoId())
                .filter(c -> c.getEventoId().equals(cmd.eventoId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Convidado não encontrado"));

        Rsvp rsvp = rsvps.buscarPorEventoEConvidado(cmd.eventoId(), cmd.convidadoId())
                .orElseGet(() -> new Rsvp(null, cmd.eventoId(), cmd.convidadoId(), null, null));
        rsvp.confirmar(cmd.nomesAcompanhantes(), convidado.getAcompanhantesPermitidos(), agora);
        Rsvp salvo = rsvps.salvar(rsvp);

        acessos.registrar(cmd.eventoId(), cmd.convidadoId(), null,
                AcaoAcessoConvidado.RSVP_CONFIRM, cmd.ip(), cmd.userAgent());
        return salvo.totalPessoas();
    }

    public record Comando(UUID eventoId, UUID convidadoId, List<String> nomesAcompanhantes,
                          String ip, String userAgent) {}
}
