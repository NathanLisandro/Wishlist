package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ColaboradorEventoRepositorioPort;
import com.wishlist.nathan.application.port.ContaRepositorioPort;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Lista os co-gestores do evento (só o dono vê). */
@Service
public class ListarColaboradores {

    private final EventoRepositorioPort eventos;
    private final ColaboradorEventoRepositorioPort colaboradores;
    private final ContaRepositorioPort contas;

    public ListarColaboradores(EventoRepositorioPort eventos,
                               ColaboradorEventoRepositorioPort colaboradores,
                               ContaRepositorioPort contas) {
        this.eventos = eventos;
        this.colaboradores = colaboradores;
        this.contas = contas;
    }

    @Transactional(readOnly = true)
    public List<Colaborador> executar(UUID contaId, UUID eventoId) {
        Evento evento = eventos.buscarPorId(eventoId)
                .filter(e -> e.getContaId().equals(contaId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        return colaboradores.listarContaIdsDoEvento(evento.getId()).stream()
                .map(id -> contas.buscarPorId(id)
                        .map(c -> new Colaborador(id, c.getEmail().valor(), c.getNomeExibicao()))
                        .orElse(new Colaborador(id, "—", "—")))
                .toList();
    }

    public record Colaborador(UUID contaId, String email, String nomeExibicao) {}
}
