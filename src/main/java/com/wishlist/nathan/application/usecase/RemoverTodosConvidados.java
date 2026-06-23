package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ConvidadoRepositorioPort;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Remove TODOS os convidados de um evento (limpar a lista e reimportar, por exemplo). */
@Service
public class RemoverTodosConvidados {

    private final AcessoEvento acessoEvento;
    private final ConvidadoRepositorioPort convidados;

    public RemoverTodosConvidados(AcessoEvento acessoEvento, ConvidadoRepositorioPort convidados) {
        this.acessoEvento = acessoEvento;
        this.convidados = convidados;
    }

    @Transactional
    public long executar(UUID contaId, UUID eventoId) {
        Evento evento = acessoEvento.exigir(contaId, eventoId);
        return convidados.apagarPorEvento(evento.getId());
    }
}
