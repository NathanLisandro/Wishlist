package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.application.port.ItemPresenteRepositorioPort;
import com.wishlist.nathan.application.port.ReservaPresenteRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import com.wishlist.nathan.domain.presente.ItemPresente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Remove um presente da lista. Se já houver reservas ativas, faz soft-delete
 * (vira HIDDEN: some da lista pública mas preserva o histórico das reservas).
 * Sem reservas, apaga de vez.
 */
@Service
public class RemoverPresente {

    private final EventoRepositorioPort eventos;
    private final ItemPresenteRepositorioPort itens;
    private final ReservaPresenteRepositorioPort reservas;

    public RemoverPresente(EventoRepositorioPort eventos, ItemPresenteRepositorioPort itens,
                           ReservaPresenteRepositorioPort reservas) {
        this.eventos = eventos;
        this.itens = itens;
        this.reservas = reservas;
    }

    @Transactional
    public void executar(UUID contaId, UUID eventoId, UUID presenteId) {
        Evento evento = eventos.buscarPorId(eventoId)
                .filter(e -> e.getContaId().equals(contaId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        ItemPresente item = itens.buscarPorId(presenteId)
                .filter(p -> p.getEventoId().equals(evento.getId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Presente não encontrado"));

        if (reservas.contarAtivasPorItem(presenteId) > 0) {
            item.ocultar();
            itens.salvar(item);
        } else {
            itens.apagar(presenteId);
        }
    }
}
