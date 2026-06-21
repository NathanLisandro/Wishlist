package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.CategoriaEventoRepositorioPort;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Dados públicos do evento por slug (sem sessão) — usados na landing/SSR para o
 * preview do Open Graph no WhatsApp. Não expõe nada sensível.
 */
@Service
public class ConsultarEventoPublico {

    private final EventoRepositorioPort eventos;
    private final CategoriaEventoRepositorioPort categorias;

    public ConsultarEventoPublico(EventoRepositorioPort eventos, CategoriaEventoRepositorioPort categorias) {
        this.eventos = eventos;
        this.categorias = categorias;
    }

    @Transactional(readOnly = true)
    public Resultado executar(String slug) {
        Evento evento = eventos.buscarPorSlug(slug)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
        String categoria = categorias.buscarPorId(evento.getCategoriaId())
                .map(c -> c.getLabel()).orElse(null);
        return new Resultado(evento.getSlug().valor(), evento.getNome(), evento.getDescricao(),
                evento.getDataEvento(), categoria, evento.isRsvpHabilitado(),
                evento.isPresenteDinheiroHabilitado(), evento.getStatus().name(),
                evento.getFotoUrl(), evento.getLocal(), evento.getMensagemCasal(),
                evento.getMensagemFinal(), evento.getMensagemAgradecimento());
    }

    public record Resultado(String slug, String nome, String descricao, LocalDate dataEvento,
                            String categoria, boolean rsvpHabilitado, boolean presenteDinheiroHabilitado,
                            String status, String fotoUrl, String local, String mensagemCasal,
                            String mensagemFinal, String mensagemAgradecimento) {}
}
