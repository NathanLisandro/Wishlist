package com.wishlist.nathan.infrastructure.security;

import com.wishlist.nathan.application.port.SenhaPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Hashing de senha do admin, delegando ao {@link PasswordEncoder} (BCrypt). */
@Component
public class ServicoSenha implements SenhaPort {

    private final PasswordEncoder encoder;

    public ServicoSenha(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String codificar(String senhaClara) {
        return encoder.encode(senhaClara);
    }

    @Override
    public boolean confere(String senhaClara, String hash) {
        return encoder.matches(senhaClara, hash);
    }
}
