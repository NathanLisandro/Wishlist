package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.dto.ArquivoUpload;
import com.wishlist.nathan.application.port.ArmazenamentoPort;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Grava (ou substitui) a imagem de fundo do site de um evento. O fundo antigo é
 * apagado do storage para não deixar órfãos. Devolve o caminho público da nova imagem.
 * Espelha {@link SalvarFotoEvento}.
 */
@Service
public class SalvarFundoEvento {

    private final EventoRepositorioPort eventos;
    private final AcessoEvento acessoEvento;
    private final ArmazenamentoPort armazenamento;

    public SalvarFundoEvento(EventoRepositorioPort eventos, AcessoEvento acessoEvento,
                             ArmazenamentoPort armazenamento) {
        this.eventos = eventos;
        this.acessoEvento = acessoEvento;
        this.armazenamento = armazenamento;
    }

    @Transactional
    public String executar(UUID contaId, UUID eventoId, ArquivoUpload arquivo) {
        Evento evento = acessoEvento.exigir(contaId, eventoId);

        String anterior = evento.getFundoUrl();
        String nova = armazenamento.salvar("eventos/" + eventoId + "/fundo",
                arquivo.nomeOriginal(), arquivo.contentType(), arquivo.conteudo());
        evento.definirFundo(nova);
        eventos.salvar(evento);

        if (anterior != null && !anterior.equals(nova)) {
            armazenamento.apagar(anterior);
        }
        return nova;
    }

    /** Remove a imagem de fundo do evento (apaga o arquivo e zera o campo). */
    @Transactional
    public void remover(UUID contaId, UUID eventoId) {
        Evento evento = acessoEvento.exigir(contaId, eventoId);
        String anterior = evento.getFundoUrl();
        evento.definirFundo(null);
        eventos.salvar(evento);
        if (anterior != null) {
            armazenamento.apagar(anterior);
        }
    }
}
