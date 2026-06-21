package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ConvidadoRepositorioPort;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.convidado.Convidado;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Remove um convidado. As FKs cuidam da cascata: reservas e RSVP (com acompanhantes)
 * são apagados; presentes em dinheiro e acessos têm o vínculo anulado (histórico mantido).
 */
@Service
public class RemoverConvidado {

    private final EventoRepositorioPort eventos;
    private final ConvidadoRepositorioPort convidados;

    public RemoverConvidado(EventoRepositorioPort eventos, ConvidadoRepositorioPort convidados) {
        this.eventos = eventos;
        this.convidados = convidados;
    }

    @Transactional
    public void executar(UUID contaId, UUID eventoId, UUID convidadoId) {
        Evento evento = eventos.buscarPorId(eventoId)
                .filter(e -> e.getContaId().equals(contaId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        Convidado convidado = convidados.buscarPorId(convidadoId)
                .filter(c -> c.getEventoId().equals(evento.getId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Convidado não encontrado"));
        convidados.apagar(convidado.getId());
    }
}
