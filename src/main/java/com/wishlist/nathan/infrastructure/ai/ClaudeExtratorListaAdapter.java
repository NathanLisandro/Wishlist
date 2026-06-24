package com.wishlist.nathan.infrastructure.ai;

import com.anthropic.models.messages.Base64ImageSource;
import com.anthropic.models.messages.Base64PdfSource;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.DocumentBlockParam;
import com.anthropic.models.messages.ImageBlockParam;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.TextBlockParam;
import com.wishlist.nathan.application.dto.EntradaIa;
import com.wishlist.nathan.application.dto.ItemPresenteExtraido;
import com.wishlist.nathan.application.port.ExtratorListaIaPort;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Extração de lista de presentes via Claude (visão + PDF nativo) com saída
 * estruturada (structured outputs) — sem parsing frágil. O system prompt é cacheado.
 */
@Component
public class ClaudeExtratorListaAdapter implements ExtratorListaIaPort {

    private static final String SYSTEM = """
            Você extrai itens de uma lista de presentes de casamento a partir do material
            fornecido (imagem/print, PDF/planilha ou texto). Regras:
            - Devolva apenas itens reais de presente (ex.: 'Jogo de panelas', 'Geladeira').
            - nome: curto e claro, sem preço nem quantidade embutidos.
            - variante: cor/material/tamanho/acabamento, se houver; senão nulo.
            - quantidade: inteiro; use 1 quando não houver indicação.
            - linkProduto: a URL do produto, se aparecer; senão nulo.
            - Ignore cabeçalhos, totais, valores, mensagens decorativas e itens duplicados.
            - Não invente itens que não estejam no material.""";

    private final ClienteClaude cliente;

    public ClaudeExtratorListaAdapter(ClienteClaude cliente) {
        this.cliente = cliente;
    }

    @Override
    public List<ItemPresenteExtraido> extrair(EntradaIa entrada) {
        List<ContentBlockParam> blocos = new ArrayList<>();
        switch (entrada.tipo()) {
            case PDF -> blocos.add(ContentBlockParam.ofDocument(DocumentBlockParam.builder()
                    .source(Base64PdfSource.builder()
                            .data(Base64.getEncoder().encodeToString(entrada.binario()))
                            .build())
                    .build()));
            case IMAGEM -> blocos.add(ContentBlockParam.ofImage(ImageBlockParam.builder()
                    .source(Base64ImageSource.builder()
                            .mediaType(midiaImagem(entrada.contentType()))
                            .data(Base64.getEncoder().encodeToString(entrada.binario()))
                            .build())
                    .build()));
            case TEXTO -> { /* sem bloco binário; o texto vai no bloco de instrução abaixo */ }
        }
        String instrucao = entrada.tipo() == EntradaIa.Tipo.TEXTO
                ? "Extraia os itens desta lista de presentes:\n\n" + entrada.texto()
                : "Extraia os itens da lista de presentes neste material.";
        blocos.add(ContentBlockParam.ofText(TextBlockParam.builder().text(instrucao).build()));

        StructuredMessageCreateParams<ListaExtraida> params = MessageCreateParams.builder()
                .model(cliente.modelo())
                .maxTokens(8000L)
                .putAdditionalHeader("anthropic-beta", "structured-outputs-2025-11-13")
                .systemOfTextBlockParams(List.of(TextBlockParam.builder()
                        .text(SYSTEM)
                        .cacheControl(CacheControlEphemeral.builder().build())
                        .build()))
                .addUserMessageOfBlockParams(blocos)
                .outputConfig(ListaExtraida.class)
                .build();

        ListaExtraida saida = cliente.get().messages().create(params).content().stream()
                .flatMap(cb -> cb.text().stream())
                .map(tb -> tb.text())
                .findFirst()
                .orElse(new ListaExtraida(List.of()));

        List<ItemIa> itens = saida.itens() == null ? List.of() : saida.itens();
        return itens.stream()
                .filter(i -> i.nome() != null && !i.nome().isBlank())
                .map(i -> new ItemPresenteExtraido(i.nome().trim(),
                        vazioParaNull(i.variante()),
                        i.quantidade() == null || i.quantidade() < 1 ? 1 : i.quantidade(),
                        vazioParaNull(i.linkProduto()), null))
                .toList();
    }

    private static Base64ImageSource.MediaType midiaImagem(String contentType) {
        String ct = contentType == null ? "" : contentType.toLowerCase();
        if (ct.contains("png")) return Base64ImageSource.MediaType.IMAGE_PNG;
        if (ct.contains("webp")) return Base64ImageSource.MediaType.IMAGE_WEBP;
        if (ct.contains("gif")) return Base64ImageSource.MediaType.IMAGE_GIF;
        return Base64ImageSource.MediaType.IMAGE_JPEG;
    }

    private static String vazioParaNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    /** Saída estruturada da extração (schema derivado pelo SDK). */
    public record ListaExtraida(List<ItemIa> itens) {}

    public record ItemIa(
            @JsonPropertyDescription("Nome curto e claro do presente") String nome,
            @JsonPropertyDescription("Cor/material/tamanho, se houver") String variante,
            @JsonPropertyDescription("Quantidade; 1 se não indicado") Integer quantidade,
            @JsonPropertyDescription("URL do produto, se houver") String linkProduto) {}
}
