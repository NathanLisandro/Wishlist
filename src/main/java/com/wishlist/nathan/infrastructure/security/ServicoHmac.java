package com.wishlist.nathan.infrastructure.security;

import com.wishlist.nathan.application.port.HmacPort;
import com.wishlist.nathan.infrastructure.config.AppProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * HMAC-SHA256 para gerar o {@code phone_hash} (lookup e rastreamento sem expor o
 * telefone). Chave de {@code PHONE_HMAC_KEY}. Determinístico: mesmo telefone -&gt;
 * mesmo hash, permitindo o UNIQUE (event_id, phone_hash).
 */
@Component
public class ServicoHmac implements HmacPort {

    private static final String ALGORITMO = "HmacSHA256";

    private final String chave;

    public ServicoHmac(AppProperties props) {
        this.chave = props.security().phoneHmacKey();
    }

    @Override
    public byte[] hash(String valor) {
        if (chave == null || chave.isBlank()) {
            throw new IllegalStateException("PHONE_HMAC_KEY não configurada");
        }
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(chave.getBytes(StandardCharsets.UTF_8), ALGORITMO));
            return mac.doFinal(valor.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao calcular HMAC", e);
        }
    }
}
