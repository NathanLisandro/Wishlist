package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.ConflitoException;
import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.CategoriaEventoRepositorioPort;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.comum.Slug;
import com.wishlist.nathan.domain.evento.Evento;
import com.wishlist.nathan.domain.evento.StatusEvento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Cria um evento na conta do admin. */
@Service
public class CriarEvento {

    private final EventoRepositorioPort eventos;
    private final CategoriaEventoRepositorioPort categorias;

    public CriarEvento(EventoRepositorioPort eventos, CategoriaEventoRepositorioPort categorias) {
        this.eventos = eventos;
        this.categorias = categorias;
    }

    @Transactional
    public UUID executar(UUID contaId, Comando cmd) {
        categorias.buscarPorId(cmd.categoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria inexistente"));
        Slug slug = new Slug(cmd.slug());
        if (eventos.existeSlugNaConta(contaId, slug.valor())) {
            throw new ConflitoException("Já existe um evento com esse slug nesta conta");
        }
        Evento evento = new Evento(null, contaId, cmd.categoriaId(), slug, cmd.nome(), cmd.descricao(),
                cmd.dataEvento(), cmd.rsvpHabilitado(), cmd.prazoRsvp(), cmd.presenteDinheiroHabilitado(),
                StatusEvento.DRAFT, null);
        return eventos.salvar(evento).getId();
    }

    public record Comando(short categoriaId, String slug, String nome, String descricao,
                          LocalDate dataEvento, boolean rsvpHabilitado, Instant prazoRsvp,
                          boolean presenteDinheiroHabilitado) {}
}
