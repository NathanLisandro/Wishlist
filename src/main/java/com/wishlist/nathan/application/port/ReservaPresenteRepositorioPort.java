package com.wishlist.nathan.application.port;

import com.wishlist.nathan.domain.presente.ReservaPresente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaPresenteRepositorioPort {

    ReservaPresente salvar(ReservaPresente reserva);

    /** Conta reservas ativas (CLAIMED) de um item — base do controle de estoque (3NF). */
    int contarAtivasPorItem(UUID itemPresenteId);

    /** Ids dos itens que o convidado reservou (ativos) neste evento — "meus presentes". */
    List<UUID> listarItensReservadosPorConvidado(UUID eventoId, UUID convidadoId);

    Optional<ReservaPresente> buscarAtivaPorItemEConvidado(UUID itemPresenteId, UUID convidadoId);

    Optional<ReservaPresente> buscarPorChaveIdempotencia(String chave);

    long contarAtivasPorEvento(UUID eventoId);
}
