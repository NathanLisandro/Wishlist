package com.wishlist.nathan.application.port;

import com.wishlist.nathan.application.dto.Pagina;
import com.wishlist.nathan.domain.convidado.Convidado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConvidadoRepositorioPort {

    Convidado salvar(Convidado convidado);

    Optional<Convidado> buscarPorId(UUID id);

    /** Lookup por telefone via HMAC (phone_hash), escopado ao evento. */
    Optional<Convidado> buscarPorEventoEPhoneHash(UUID eventoId, byte[] phoneHash);

    long contarPorEvento(UUID eventoId);

    /** Todos os convidados do evento (gestão/lista de presença no painel). */
    List<Convidado> listarPorEvento(UUID eventoId);

    /** Página de convidados do evento, ordenados por criação — painel do admin. */
    Pagina<Convidado> listarPorEventoPaginado(UUID eventoId, int page, int size);

    /** Remove um convidado (cascata limpa reservas/RSVP; dinheiro/acessos viram NULL). */
    void apagar(UUID id);

    /** Remove TODOS os convidados do evento. Devolve quantos foram apagados. */
    long apagarPorEvento(UUID eventoId);
}
