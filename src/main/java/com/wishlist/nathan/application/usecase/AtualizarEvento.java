package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Edita os campos do evento que o casal personaliza (nome, data, local, recado, flags). */
@Service
public class AtualizarEvento {

    private final EventoRepositorioPort eventos;
    private final AcessoEvento acessoEvento;

    public AtualizarEvento(EventoRepositorioPort eventos, AcessoEvento acessoEvento) {
        this.eventos = eventos;
        this.acessoEvento = acessoEvento;
    }

    @Transactional
    public void executar(UUID contaId, UUID eventoId, Comando cmd) {
        Evento evento = acessoEvento.exigir(contaId, eventoId);
        evento.editar(cmd.nome(), cmd.descricao(), cmd.dataEvento(), cmd.local(), cmd.mensagemCasal(),
                cmd.mensagemFinal(), cmd.mensagemAgradecimento(), cmd.rsvpHabilitado(), cmd.prazoRsvp(),
                cmd.presenteDinheiroHabilitado());
        eventos.salvar(evento);
    }

    public record Comando(String nome, String descricao, LocalDate dataEvento, String local,
                          String mensagemCasal, String mensagemFinal, String mensagemAgradecimento,
                          boolean rsvpHabilitado, Instant prazoRsvp, boolean presenteDinheiroHabilitado) {}
}
