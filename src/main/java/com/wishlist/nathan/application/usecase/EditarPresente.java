package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.ConflitoException;
import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.application.port.ItemPresenteRepositorioPort;
import com.wishlist.nathan.application.port.ReservaPresenteRepositorioPort;
import com.wishlist.nathan.domain.comum.Quantidade;
import com.wishlist.nathan.domain.evento.Evento;
import com.wishlist.nathan.domain.presente.ItemPresente;
import com.wishlist.nathan.domain.presente.StatusPresente;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Edita um presente existente (nome, variante, quantidade, link, imagem). Recalcula
 * o estado de esgotamento conforme as reservas ativas vs. a nova quantidade, e a
 * natural_key (idempotência de import) a partir do novo nome/variante.
 */
@Service
public class EditarPresente {

    private final AcessoEvento acessoEvento;
    private final ItemPresenteRepositorioPort itens;
    private final ReservaPresenteRepositorioPort reservas;

    public EditarPresente(AcessoEvento acessoEvento, ItemPresenteRepositorioPort itens,
                          ReservaPresenteRepositorioPort reservas) {
        this.acessoEvento = acessoEvento;
        this.itens = itens;
        this.reservas = reservas;
    }

    @Transactional
    public void executar(UUID contaId, UUID eventoId, UUID presenteId, Comando cmd) {
        Evento evento = acessoEvento.exigirGestaoPresentes(contaId, eventoId);
        ItemPresente atual = itens.buscarPorId(presenteId)
                .filter(p -> p.getEventoId().equals(evento.getId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Presente não encontrado"));

        int reservados = reservas.contarAtivasPorItem(presenteId);
        if (cmd.quantidade() < reservados) {
            throw new ConflitoException("A quantidade não pode ser menor que as "
                    + reservados + " reserva(s) já feitas.");
        }
        StatusPresente status = reservados >= cmd.quantidade()
                ? StatusPresente.SOLD_OUT : StatusPresente.AVAILABLE;

        ItemPresente editado = new ItemPresente(atual.getId(), evento.getId(), cmd.nome(), cmd.variante(),
                new Quantidade(cmd.quantidade()), cmd.linkProduto(), cmd.imagemUrl(), status,
                chaveNatural(cmd.nome(), cmd.variante()), atual.getCriadoEm());
        editado.definirPrecoEstimado(atual.getPrecoEstimado()); // preserva a estimativa da IA
        try {
            itens.salvar(editado);
        } catch (DataIntegrityViolationException e) {
            throw new ConflitoException("Já existe outro presente equivalente neste evento");
        }
    }

    private static String chaveNatural(String nome, String variante) {
        String base = (nome == null ? "" : nome.trim().toLowerCase())
                + "|" + (variante == null ? "" : variante.trim().toLowerCase());
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(base.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar natural_key", e);
        }
    }

    public record Comando(String nome, String variante, int quantidade, String linkProduto, String imagemUrl) {}
}
