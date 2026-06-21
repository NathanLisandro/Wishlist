package com.wishlist.nathan.application.port;

/**
 * Port de HMAC para lookup de telefone sem expor o número (phone_hash).
 */
public interface HmacPort {

    byte[] hash(String valor);
}
