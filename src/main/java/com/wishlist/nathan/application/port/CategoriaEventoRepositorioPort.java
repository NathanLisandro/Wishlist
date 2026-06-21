package com.wishlist.nathan.application.port;

import com.wishlist.nathan.domain.evento.CategoriaEvento;

import java.util.List;
import java.util.Optional;

public interface CategoriaEventoRepositorioPort {

    List<CategoriaEvento> listarAtivas();

    Optional<CategoriaEvento> buscarPorId(short id);
}
