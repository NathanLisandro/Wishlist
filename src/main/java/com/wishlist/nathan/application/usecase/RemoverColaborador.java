package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ColaboradorEventoRepositorioPort;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Remove um co-gestor do evento (só o dono pode). */
@Service
public class RemoverColaborador {

    private final EventoRepositorioPort eventos;
    private final ColaboradorEventoRepositorioPort colaboradores;

    public RemoverColaborador(EventoRepositorioPort eventos,
                              ColaboradorEventoRepositorioPort colaboradores) {
        this.eventos = eventos;
        this.colaboradores = colaboradores;
    }

    @Transactional
    public void executar(UUID contaId, UUID eventoId, UUID colaboradorContaId) {
        Evento evento = eventos.buscarPorId(eventoId)
                .filter(e -> e.getContaId().equals(contaId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        colaboradores.remover(evento.getId(), colaboradorContaId);
    }
}
