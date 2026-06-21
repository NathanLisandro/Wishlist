package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.ConflitoException;
import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.application.port.ItemPresenteRepositorioPort;
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

/** Adiciona um item de presente a um evento do admin (alternativa direta ao import CSV). */
@Service
public class AdicionarPresente {

    private final EventoRepositorioPort eventos;
    private final ItemPresenteRepositorioPort itens;

    public AdicionarPresente(EventoRepositorioPort eventos, ItemPresenteRepositorioPort itens) {
        this.eventos = eventos;
        this.itens = itens;
    }

    @Transactional
    public UUID executar(UUID contaId, UUID eventoId, Comando cmd) {
        Evento evento = eventos.buscarPorId(eventoId)
                .filter(e -> e.getContaId().equals(contaId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        String naturalKey = chaveNatural(cmd.nome(), cmd.variante());
        ItemPresente item = new ItemPresente(null, evento.getId(), cmd.nome(), cmd.variante(),
                new Quantidade(cmd.quantidade()), cmd.linkProduto(), cmd.imagemUrl(),
                StatusPresente.AVAILABLE, naturalKey, null);
        try {
            return itens.salvar(item).getId();
        } catch (DataIntegrityViolationException e) {
            throw new ConflitoException("Já existe um presente equivalente neste evento");
        }
    }

    /** Hash estável de (nome|variante) para idempotência de importação. */
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
