package com.wishlist.nathan.application.port;

import com.wishlist.nathan.domain.convidado.AcaoAcessoConvidado;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Rastreamento de acessos do convidado (guest_access_log). Nunca recebe o
 * telefone em claro — apenas {@code phoneHash} (HMAC) e/ou {@code guestId}.
 */
public interface RegistroAcessoPort {

    void registrar(UUID eventoId, UUID convidadoId, byte[] phoneHash,
                   AcaoAcessoConvidado acao, String ip, String userAgent);

    /** Acessos recentes do evento (para o painel do admin). */
    List<AcessoRecente> listarRecentes(UUID eventoId);

    record AcessoRecente(AcaoAcessoConvidado acao, UUID convidadoId, String ip, Instant quando) {}
}
