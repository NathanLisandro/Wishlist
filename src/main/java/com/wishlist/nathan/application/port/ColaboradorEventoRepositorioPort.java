package com.wishlist.nathan.application.port;

import java.util.List;
import java.util.UUID;

/** Contas que co-gerenciam um evento (além do dono). */
public interface ColaboradorEventoRepositorioPort {

    /** Adiciona a conta como co-gestora do evento (idempotente). */
    void adicionar(UUID eventoId, UUID contaId);

    boolean existe(UUID eventoId, UUID contaId);

    /** Remove a co-gestão da conta no evento. */
    void remover(UUID eventoId, UUID contaId);

    /** Ids dos eventos que a conta co-gerencia (não inclui os que ela é dona). */
    List<UUID> listarEventoIdsDoColaborador(UUID contaId);

    /** Ids das contas que co-gerenciam o evento. */
    List<UUID> listarContaIdsDoEvento(UUID eventoId);
}
