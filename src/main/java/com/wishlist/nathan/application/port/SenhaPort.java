package com.wishlist.nathan.application.port;

/**
 * Port de hashing de senha do admin (BCrypt/Argon2 na infraestrutura).
 */
public interface SenhaPort {

    String codificar(String senhaClara);

    boolean confere(String senhaClara, String hash);
}
