package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ArmazenamentoPort;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Grava (ou substitui) a foto do casal de um evento. A foto antiga é apagada do
 * storage para não deixar órfãos. Devolve o caminho público da nova imagem.
 */
@Service
public class SalvarFotoEvento {

    private final EventoRepositorioPort eventos;
    private final ArmazenamentoPort armazenamento;

    public SalvarFotoEvento(EventoRepositorioPort eventos, ArmazenamentoPort armazenamento) {
        this.eventos = eventos;
        this.armazenamento = armazenamento;
    }

    @Transactional
    public String executar(UUID contaId, UUID eventoId, String nomeOriginal,
                           String contentType, byte[] conteudo) {
        Evento evento = eventos.buscarPorId(eventoId)
                .filter(e -> e.getContaId().equals(contaId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));

        String anterior = evento.getFotoUrl();
        String nova = armazenamento.salvar("eventos/" + eventoId, nomeOriginal, contentType, conteudo);
        evento.definirFoto(nova);
        eventos.salvar(evento);

        if (anterior != null && !anterior.equals(nova)) {
            armazenamento.apagar(anterior);
        }
        return nova;
    }
}
