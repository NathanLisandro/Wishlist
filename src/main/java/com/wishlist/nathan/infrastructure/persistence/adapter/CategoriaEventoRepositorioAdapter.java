package com.wishlist.nathan.infrastructure.persistence.adapter;

import com.wishlist.nathan.application.port.CategoriaEventoRepositorioPort;
import com.wishlist.nathan.domain.evento.CategoriaEvento;
import com.wishlist.nathan.infrastructure.persistence.entity.CategoriaEventoJpa;
import com.wishlist.nathan.infrastructure.persistence.repository.CategoriaEventoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CategoriaEventoRepositorioAdapter implements CategoriaEventoRepositorioPort {

    private final CategoriaEventoJpaRepository repo;

    public CategoriaEventoRepositorioAdapter(CategoriaEventoJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<CategoriaEvento> listarAtivas() {
        return repo.findByActiveTrue().stream().map(this::paraDominio).toList();
    }

    @Override
    public Optional<CategoriaEvento> buscarPorId(short id) {
        return repo.findById(id).map(this::paraDominio);
    }

    private CategoriaEvento paraDominio(CategoriaEventoJpa j) {
        return new CategoriaEvento(j.getId(), j.getCode(), j.getLabel(), j.isActive());
    }
}
