package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.application.port.ItemPresenteRepositorioPort;
import com.wishlist.nathan.application.port.ReservaPresenteRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import com.wishlist.nathan.domain.presente.ItemPresente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Lista todos os presentes do evento para o painel do admin, com quantos já foram
 * reservados (derivado via COUNT). Inclui itens esgotados; exclui os removidos.
 */
@Service
public class ListarPresentesAdmin {

    private final EventoRepositorioPort eventos;
    private final ItemPresenteRepositorioPort itens;
    private final ReservaPresenteRepositorioPort reservas;

    public ListarPresentesAdmin(EventoRepositorioPort eventos, ItemPresenteRepositorioPort itens,
                                ReservaPresenteRepositorioPort reservas) {
        this.eventos = eventos;
        this.itens = itens;
        this.reservas = reservas;
    }

    @Transactional(readOnly = true)
    public List<ItemAdmin> executar(UUID contaId, UUID eventoId) {
        Evento evento = eventos.buscarPorId(eventoId)
                .filter(e -> e.getContaId().equals(contaId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        return itens.listarDoEvento(evento.getId()).stream().map(this::montar).toList();
    }

    private ItemAdmin montar(ItemPresente item) {
        int reservados = reservas.contarAtivasPorItem(item.getId());
        return new ItemAdmin(item.getId(), item.getNome(), item.getVariante(),
                item.getQuantidadeTotal().valor(), reservados, item.getLinkProduto(),
                item.getImagemUrl(), item.getStatus().name());
    }

    public record ItemAdmin(UUID id, String nome, String variante, int quantidadeTotal,
                            int reservados, String linkProduto, String imagemUrl, String status) {}
}
