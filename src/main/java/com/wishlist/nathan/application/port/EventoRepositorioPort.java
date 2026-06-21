package com.wishlist.nathan.application.port;

import com.wishlist.nathan.domain.evento.Evento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventoRepositorioPort {

    Evento salvar(Evento evento);

    Optional<Evento> buscarPorId(UUID id);

    /** Busca pública pelo slug. Ver limitação de slug por conta no SDD §4. */
    Optional<Evento> buscarPorSlug(String slug);

    List<Evento> listarPorConta(UUID contaId);

    boolean existeSlugNaConta(UUID contaId, String slug);
}
