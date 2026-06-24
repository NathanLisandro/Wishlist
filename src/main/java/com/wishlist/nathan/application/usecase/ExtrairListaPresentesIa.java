package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.dto.ArquivoUpload;
import com.wishlist.nathan.application.dto.EntradaIa;
import com.wishlist.nathan.application.dto.ItemPresenteExtraido;
import com.wishlist.nathan.application.port.BuscadorHtmlPort;
import com.wishlist.nathan.application.port.EstimadorPrecoIaPort;
import com.wishlist.nathan.application.port.ExtratorListaIaPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Lê uma lista de presentes já existente do casal (foto/print, PDF/planilha, texto
 * colado ou link de outro site) via IA e devolve os itens extraídos — com preço
 * estimado opcional — para o casal revisar antes de confirmar. NÃO grava nada aqui;
 * a materialização fica em {@link ConfirmarImportacaoIa}.
 */
@Service
public class ExtrairListaPresentesIa {

    private final AcessoEvento acessoEvento;
    private final ExtratorListaIaPort extrator;
    private final EstimadorPrecoIaPort estimador;
    private final BuscadorHtmlPort buscador;

    public ExtrairListaPresentesIa(AcessoEvento acessoEvento, ExtratorListaIaPort extrator,
                                   EstimadorPrecoIaPort estimador, BuscadorHtmlPort buscador) {
        this.acessoEvento = acessoEvento;
        this.extrator = extrator;
        this.estimador = estimador;
        this.buscador = buscador;
    }

    public List<ItemPresenteExtraido> deArquivo(UUID contaId, UUID eventoId, ArquivoUpload arquivo,
                                                boolean estimarPreco) {
        String ct = arquivo.contentType() == null ? "" : arquivo.contentType().toLowerCase();
        EntradaIa entrada = ct.contains("pdf")
                ? EntradaIa.pdf(arquivo.conteudo())
                : EntradaIa.imagem(arquivo.conteudo(), arquivo.contentType());
        return extrair(contaId, eventoId, entrada, estimarPreco);
    }

    public List<ItemPresenteExtraido> deTexto(UUID contaId, UUID eventoId, String texto,
                                              boolean estimarPreco) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Cole o texto da lista.");
        }
        return extrair(contaId, eventoId, EntradaIa.texto(texto), estimarPreco);
    }

    public List<ItemPresenteExtraido> deLink(UUID contaId, UUID eventoId, String url,
                                             boolean estimarPreco) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Informe o link da lista.");
        }
        String texto = buscador.buscarTexto(url);
        return extrair(contaId, eventoId, EntradaIa.texto(texto), estimarPreco);
    }

    private List<ItemPresenteExtraido> extrair(UUID contaId, UUID eventoId, EntradaIa entrada,
                                               boolean estimarPreco) {
        acessoEvento.exigirGestaoPresentes(contaId, eventoId);
        List<ItemPresenteExtraido> itens = extrator.extrair(entrada);
        if (!estimarPreco || itens.isEmpty()) {
            return itens;
        }
        Map<String, BigDecimal> precos = estimador.estimar(itens.stream().map(ItemPresenteExtraido::nome).toList());
        return itens.stream()
                .map(i -> new ItemPresenteExtraido(i.nome(), i.variante(), i.quantidade(),
                        i.linkProduto(), precos.get(i.nome())))
                .toList();
    }
}
