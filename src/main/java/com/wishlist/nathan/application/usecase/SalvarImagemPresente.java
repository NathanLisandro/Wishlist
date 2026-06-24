package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.dto.ArquivoUpload;
import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ArmazenamentoPort;
import com.wishlist.nathan.application.port.ItemPresenteRepositorioPort;
import com.wishlist.nathan.domain.evento.Evento;
import com.wishlist.nathan.domain.presente.ItemPresente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Grava (ou substitui) a imagem de um presente a partir de um upload (ex.: foto
 * tirada pelo celular). Apaga a imagem anterior apenas quando era um arquivo nosso
 * (caminho de upload), nunca uma URL externa que o casal tenha colado.
 */
@Service
public class SalvarImagemPresente {

    private final AcessoEvento acessoEvento;
    private final ItemPresenteRepositorioPort itens;
    private final ArmazenamentoPort armazenamento;

    public SalvarImagemPresente(AcessoEvento acessoEvento, ItemPresenteRepositorioPort itens,
                                ArmazenamentoPort armazenamento) {
        this.acessoEvento = acessoEvento;
        this.itens = itens;
        this.armazenamento = armazenamento;
    }

    @Transactional
    public String executar(UUID contaId, UUID eventoId, UUID presenteId, ArquivoUpload arquivo) {
        Evento evento = acessoEvento.exigirGestaoPresentes(contaId, eventoId);
        ItemPresente item = itens.buscarPorId(presenteId)
                .filter(p -> p.getEventoId().equals(evento.getId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Presente não encontrado"));

        String anterior = item.getImagemUrl();
        String nova = armazenamento.salvar("eventos/" + eventoId + "/presentes/" + presenteId,
                arquivo.nomeOriginal(), arquivo.contentType(), arquivo.conteudo());
        item.definirImagem(nova);
        itens.salvar(item);

        boolean anteriorEraUpload = anterior != null && !anterior.equals(nova)
                && !anterior.startsWith("http");
        if (anteriorEraUpload) {
            armazenamento.apagar(anterior);
        }
        return nova;
    }
}
