package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prévia pública do convite de co-gestão pelo token (para a tela mostrar de qual
 * evento é antes da pessoa entrar/criar conta). O token é o segredo.
 */
@Service
public class ConsultarConvite {

    private final EventoRepositorioPort eventos;

    public ConsultarConvite(EventoRepositorioPort eventos) {
        this.eventos = eventos;
    }

    @Transactional(readOnly = true)
    public Resultado executar(String token) {
        Evento evento = eventos.buscarPorTokenConviteGestao(token)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Convite inválido ou expirado"));
        return new Resultado(evento.getNome(), evento.getSlug().valor());
    }

    public record Resultado(String nomeEvento, String slug) {}
}
