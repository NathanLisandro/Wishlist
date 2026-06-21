package com.wishlist.nathan.application.port;

import com.wishlist.nathan.application.dto.SessaoAdmin;
import com.wishlist.nathan.application.dto.SessaoConvidado;

import java.util.UUID;

/**
 * Port de emissão/validação de tokens de sessão (JWT na infraestrutura).
 */
public interface TokenSessaoPort {

    String emitirTokenAdmin(UUID contaId);

    String emitirTokenConvidado(UUID eventoId, UUID convidadoId);

    /** @throws SessaoInvalidaException se o token for inválido/expirado ou de outro tipo. */
    SessaoAdmin validarAdmin(String token);

    /** @throws SessaoInvalidaException se o token for inválido/expirado ou de outro tipo. */
    SessaoConvidado validarConvidado(String token);

    class SessaoInvalidaException extends RuntimeException {
        public SessaoInvalidaException(String mensagem) {
            super(mensagem);
        }
    }
}
