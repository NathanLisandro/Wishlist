package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.application.port.ItemPresenteRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Remove TODOS os presentes de um evento (limpar a lista e reimportar, por exemplo). */
@Service
public class RemoverTodosPresentes {

    private final AcessoEvento acessoEvento;
    private final ItemPresenteRepositorioPort itens;

    public RemoverTodosPresentes(AcessoEvento acessoEvento, ItemPresenteRepositorioPort itens) {
        this.acessoEvento = acessoEvento;
        this.itens = itens;
    }

    @Transactional
    public long executar(UUID contaId, UUID eventoId) {
        Evento evento = acessoEvento.exigirGestaoPresentes(contaId, eventoId);
        return itens.apagarPorEvento(evento.getId());
    }
}
