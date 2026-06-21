package com.wishlist.nathan.infrastructure.security;

import com.wishlist.nathan.application.port.CriptografiaPort;
import com.wishlist.nathan.infrastructure.config.AppProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Criptografia de PII com AES-256-GCM. A chave vem de {@code PII_ENCRYPTION_KEY}
 * (base64 de 32 bytes). O IV (12 bytes) é aleatório por operação e gravado junto
 * do ciphertext: {@code [IV | ciphertext+tag]}.
 */
@Component
public class ServicoCriptografia implements CriptografiaPort {

    private static final int TAM_IV = 12;       // recomendado para GCM
    private static final int TAM_TAG_BITS = 128;

    private final String chaveBase64;
    private final SecureRandom random = new SecureRandom();
    private volatile SecretKey chave;

    public ServicoCriptografia(AppProperties props) {
        this.chaveBase64 = props.security().piiEncryptionKey();
    }

    @Override
    public byte[] criptografar(String textoClaro) {
        try {
            byte[] iv = new byte[TAM_IV];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, chave(), new GCMParameterSpec(TAM_TAG_BITS, iv));
            byte[] cifrado = cipher.doFinal(textoClaro.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.allocate(iv.length + cifrado.length).put(iv).put(cifrado).array();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao criptografar", e);
        }
    }

    @Override
    public String descriptografar(byte[] cifrado) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(cifrado);
            byte[] iv = new byte[TAM_IV];
            buffer.get(iv);
            byte[] dados = new byte[buffer.remaining()];
            buffer.get(dados);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, chave(), new GCMParameterSpec(TAM_TAG_BITS, iv));
            return new String(cipher.doFinal(dados), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao descriptografar", e);
        }
    }

    /** Carrega a chave sob demanda (permite o contexto subir mesmo sem a env var em dev). */
    private SecretKey chave() {
        SecretKey local = chave;
        if (local == null) {
            synchronized (this) {
                if (chave == null) {
                    if (chaveBase64 == null || chaveBase64.isBlank()) {
                        throw new IllegalStateException("PII_ENCRYPTION_KEY não configurada");
                    }
                    byte[] bytes = Base64.getDecoder().decode(chaveBase64);
                    if (bytes.length != 32) {
                        throw new IllegalStateException("PII_ENCRYPTION_KEY deve ter 32 bytes (base64 de 256 bits)");
                    }
                    chave = new SecretKeySpec(bytes, "AES");
                }
                local = chave;
            }
        }
        return local;
    }
}
