package com.wishlist.nathan.application.port;

/**
 * Port de criptografia de PII em repouso (envelope encryption).
 * Implementado na infraestrutura (AES-GCM com chave de env var).
 */
public interface CriptografiaPort {

    byte[] criptografar(String textoClaro);

    String descriptografar(byte[] cifrado);
}
