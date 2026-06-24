package com.wishlist.nathan.infrastructure.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.wishlist.nathan.infrastructure.config.AppProperties;
import org.springframework.stereotype.Component;

/**
 * Fábrica/cache do cliente da Anthropic. Constrói o cliente de forma preguiçosa a
 * partir da {@code ANTHROPIC_API_KEY}; se a chave não estiver configurada, falha
 * com mensagem amigável (a feature de IA fica indisponível, sem derrubar o app).
 */
@Component
public class ClienteClaude {

    private static final String MODELO_PADRAO = "claude-opus-4-8";

    private final AppProperties props;
    private volatile AnthropicClient client;

    public ClienteClaude(AppProperties props) {
        this.props = props;
    }

    public AnthropicClient get() {
        AppProperties.Ai ai = props.ai();
        if (ai == null || !ai.habilitado() || ai.apiKey() == null || ai.apiKey().isBlank()) {
            throw new IaIndisponivelException();
        }
        AnthropicClient c = client;
        if (c == null) {
            synchronized (this) {
                c = client;
                if (c == null) {
                    c = AnthropicOkHttpClient.builder().apiKey(ai.apiKey()).build();
                    client = c;
                }
            }
        }
        return c;
    }

    public String modelo() {
        AppProperties.Ai ai = props.ai();
        if (ai != null && ai.model() != null && !ai.model().isBlank()) {
            return ai.model();
        }
        return MODELO_PADRAO;
    }

    /** A importação por IA não está configurada (sem API key). Mapeada para 503/erro amigável. */
    public static class IaIndisponivelException extends RuntimeException {
        public IaIndisponivelException() {
            super("Importação por IA indisponível: configure a chave da Anthropic (ANTHROPIC_API_KEY).");
        }
    }
}
