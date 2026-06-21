package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.port.ItemPresenteRepositorioPort;
import com.wishlist.nathan.application.port.ReservaPresenteRepositorioPort;
import com.wishlist.nathan.domain.presente.ItemPresente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Lista os presentes disponíveis do evento, com vagas restantes (derivadas via COUNT). */
@Service
public class ListarPresentes {

    private final ItemPresenteRepositorioPort itens;
    private final ReservaPresenteRepositorioPort reservas;

    public ListarPresentes(ItemPresenteRepositorioPort itens, ReservaPresenteRepositorioPort reservas) {
        this.itens = itens;
        this.reservas = reservas;
    }

    @Transactional(readOnly = true)
    public List<ItemDisponivel> executar(UUID eventoId) {
        return executar(eventoId, null);
    }

    /**
     * Lista os itens disponíveis; quando {@code convidadoId} é informado, marca os
     * que o próprio convidado já reservou ({@code reservadoPorMim}).
     */
    @Transactional(readOnly = true)
    public List<ItemDisponivel> executar(UUID eventoId, UUID convidadoId) {
        Set<UUID> meus = convidadoId == null ? Set.of()
                : Set.copyOf(reservas.listarItensReservadosPorConvidado(eventoId, convidadoId));
        return itens.listarDisponiveis(eventoId).stream()
                .map(item -> montar(item, meus.contains(item.getId())))
                .toList();
    }

    /**
     * "Meus presentes": os itens que o convidado reservou (inclui itens já esgotados,
     * que somem da lista pública mas ele ainda precisa poder ver/cancelar).
     */
    @Transactional(readOnly = true)
    public List<ItemDisponivel> meusPresentes(UUID eventoId, UUID convidadoId) {
        return reservas.listarItensReservadosPorConvidado(eventoId, convidadoId).stream()
                .map(itens::buscarPorId)
                .flatMap(java.util.Optional::stream)
                .map(item -> montar(item, true))
                .toList();
    }

    private ItemDisponivel montar(ItemPresente item, boolean reservadoPorMim) {
        int ativas = reservas.contarAtivasPorItem(item.getId());
        int restantes = Math.max(0, item.getQuantidadeTotal().valor() - ativas);
        return new ItemDisponivel(item.getId(), item.getNome(), item.getVariante(),
                item.getQuantidadeTotal().valor(), restantes, item.getLinkProduto(),
                item.getImagemUrl(), reservadoPorMim);
    }

    public record ItemDisponivel(UUID id, String nome, String variante, int quantidadeTotal,
                                 int vagasRestantes, String linkProduto, String imagemUrl,
                                 boolean reservadoPorMim) {}
}
