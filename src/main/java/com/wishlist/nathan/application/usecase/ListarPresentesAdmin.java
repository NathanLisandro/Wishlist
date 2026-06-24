package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.dto.Pagina;
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

    private final AcessoEvento acessoEvento;
    private final ItemPresenteRepositorioPort itens;
    private final ReservaPresenteRepositorioPort reservas;

    public ListarPresentesAdmin(AcessoEvento acessoEvento, ItemPresenteRepositorioPort itens,
                                ReservaPresenteRepositorioPort reservas) {
        this.acessoEvento = acessoEvento;
        this.itens = itens;
        this.reservas = reservas;
    }

    @Transactional(readOnly = true)
    public List<ItemAdmin> executar(UUID contaId, UUID eventoId) {
        Evento evento = acessoEvento.exigir(contaId, eventoId);
        return itens.listarDoEvento(evento.getId()).stream().map(this::montar).toList();
    }

    @Transactional(readOnly = true)
    public Pagina<ItemAdmin> executarPaginado(UUID contaId, UUID eventoId, int page, int size) {
        Evento evento = acessoEvento.exigir(contaId, eventoId);
        return itens.listarDoEventoPaginado(evento.getId(), page, size).mapear(this::montar);
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
