package com.wishlist.nathan.application.port;

import com.wishlist.nathan.domain.presente.ItemPresente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemPresenteRepositorioPort {

    ItemPresente salvar(ItemPresente item);

    Optional<ItemPresente> buscarPorId(UUID id);

    /**
     * Busca o item com lock de linha (SELECT ... FOR UPDATE) para a reserva
     * idempotente sem oversell (ver SDD §6.1). Use dentro de uma transação.
     */
    Optional<ItemPresente> buscarPorIdComLock(UUID id);

    /** Itens AVAILABLE do evento (lista pública). */
    List<ItemPresente> listarDisponiveis(UUID eventoId);

    /** Todos os itens do evento exceto os ocultados/removidos (HIDDEN) — gestão do admin. */
    List<ItemPresente> listarDoEvento(UUID eventoId);

    /** Remove fisicamente um item (usado quando não há reservas). */
    void apagar(UUID id);

    /** Remove TODOS os presentes do evento (cascata limpa as reservas). Devolve quantos. */
    long apagarPorEvento(UUID eventoId);
}
