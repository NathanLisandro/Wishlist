package com.wishlist.nathan.infrastructure.ai;

import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.TextBlockParam;
import com.wishlist.nathan.application.port.EstimadorPrecoIaPort;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Estima o preço médio de mercado (BRL) de cada item pelo nome, via Claude com
 * saída estruturada. Usa o conhecimento do modelo sobre preços de varejo no Brasil
 * (estimativa, não cotação em tempo real). Uma única chamada para a lista toda.
 */
@Component
public class ClaudeEstimadorPrecoAdapter implements EstimadorPrecoIaPort {

    private static final String SYSTEM = """
            Você estima o preço médio atual de varejo no Brasil (em reais, BRL) de itens de
            lista de presentes de casamento. Para cada nome recebido, devolva um preço único
            aproximado (número, sem símbolo), realista para um produto de entrada/intermediário.
            Se não tiver ideia razoável do preço, omita o item.""";

    private final ClienteClaude cliente;

    public ClaudeEstimadorPrecoAdapter(ClienteClaude cliente) {
        this.cliente = cliente;
    }

    @Override
    public Map<String, BigDecimal> estimar(List<String> nomes) {
        if (nomes == null || nomes.isEmpty()) {
            return Map.of();
        }
        String lista = String.join("\n", nomes.stream().map(n -> "- " + n).toList());

        StructuredMessageCreateParams<Estimativas> params = MessageCreateParams.builder()
                .model(cliente.modelo())
                .maxTokens(4000L)
                .putAdditionalHeader("anthropic-beta", "structured-outputs-2025-11-13")
                .systemOfTextBlockParams(List.of(TextBlockParam.builder()
                        .text(SYSTEM)
                        .cacheControl(CacheControlEphemeral.builder().build())
                        .build()))
                .addUserMessage("Estime o preço (BRL) de cada item:\n" + lista)
                .outputConfig(Estimativas.class)
                .build();

        Estimativas saida = cliente.get().messages().create(params).content().stream()
                .flatMap(cb -> cb.text().stream())
                .map(tb -> tb.text())
                .findFirst()
                .orElse(new Estimativas(List.of()));

        Map<String, BigDecimal> mapa = new LinkedHashMap<>();
        if (saida.precos() != null) {
            for (Estimativa e : saida.precos()) {
                if (e.nome() != null && e.precoBrl() != null && e.precoBrl() > 0) {
                    mapa.put(e.nome().trim(), BigDecimal.valueOf(e.precoBrl()));
                }
            }
        }
        return mapa;
    }

    public record Estimativas(List<Estimativa> precos) {}

    public record Estimativa(
            @JsonPropertyDescription("Nome do item, igual ao recebido") String nome,
            @JsonPropertyDescription("Preço médio de varejo no Brasil, em reais") Double precoBrl) {}
}
