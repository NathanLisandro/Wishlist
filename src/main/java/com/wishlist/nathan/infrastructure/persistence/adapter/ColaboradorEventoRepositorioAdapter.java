package com.wishlist.nathan.infrastructure.persistence.adapter;

import com.wishlist.nathan.application.port.ColaboradorEventoRepositorioPort;
import com.wishlist.nathan.infrastructure.persistence.entity.ColaboradorEventoJpa;
import com.wishlist.nathan.infrastructure.persistence.repository.ColaboradorEventoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ColaboradorEventoRepositorioAdapter implements ColaboradorEventoRepositorioPort {

    private final ColaboradorEventoJpaRepository repo;

    public ColaboradorEventoRepositorioAdapter(ColaboradorEventoJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public void adicionar(UUID eventoId, UUID contaId) {
        if (repo.existsByEventIdAndAccountId(eventoId, contaId)) {
            return; // idempotente
        }
        ColaboradorEventoJpa jpa = new ColaboradorEventoJpa();
        jpa.setId(UUID.randomUUID());
        jpa.setEventId(eventoId);
        jpa.setAccountId(contaId);
        repo.save(jpa);
    }

    @Override
    public boolean existe(UUID eventoId, UUID contaId) {
        return repo.existsByEventIdAndAccountId(eventoId, contaId);
    }

    @Override
    public void remover(UUID eventoId, UUID contaId) {
        repo.deleteByEventIdAndAccountId(eventoId, contaId);
    }

    @Override
    public List<UUID> listarEventoIdsDoColaborador(UUID contaId) {
        return repo.findByAccountId(contaId).stream().map(ColaboradorEventoJpa::getEventId).toList();
    }

    @Override
    public List<UUID> listarContaIdsDoEvento(UUID eventoId) {
        return repo.findByEventId(eventoId).stream().map(ColaboradorEventoJpa::getAccountId).toList();
    }
}
