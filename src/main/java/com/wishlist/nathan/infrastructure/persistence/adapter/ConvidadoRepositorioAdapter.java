package com.wishlist.nathan.infrastructure.persistence.adapter;

import com.wishlist.nathan.application.port.ConvidadoRepositorioPort;
import com.wishlist.nathan.domain.comum.NomeCriptografado;
import com.wishlist.nathan.domain.convidado.Convidado;
import com.wishlist.nathan.infrastructure.persistence.entity.ConvidadoJpa;
import com.wishlist.nathan.infrastructure.persistence.repository.ConvidadoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ConvidadoRepositorioAdapter implements ConvidadoRepositorioPort {

    private final ConvidadoJpaRepository repo;

    public ConvidadoRepositorioAdapter(ConvidadoJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Convidado salvar(Convidado convidado) {
        ConvidadoJpa jpa = new ConvidadoJpa();
        jpa.setId(convidado.getId() != null ? convidado.getId() : UUID.randomUUID());
        jpa.setEventId(convidado.getEventoId());
        jpa.setFullNameEncrypted(convidado.getNomeCifrado().conteudo());
        jpa.setPhoneEncrypted(convidado.getTelefoneCifrado().conteudo());
        jpa.setPhoneHash(convidado.getPhoneHash());
        jpa.setCompanionsAllowed(convidado.getAcompanhantesPermitidos());
        return paraDominio(repo.save(jpa));
    }

    @Override
    public Optional<Convidado> buscarPorId(UUID id) {
        return repo.findById(id).map(this::paraDominio);
    }

    @Override
    public Optional<Convidado> buscarPorEventoEPhoneHash(UUID eventoId, byte[] phoneHash) {
        return repo.findByEventIdAndPhoneHash(eventoId, phoneHash).map(this::paraDominio);
    }

    @Override
    public long contarPorEvento(UUID eventoId) {
        return repo.countByEventId(eventoId);
    }

    @Override
    public List<Convidado> listarPorEvento(UUID eventoId) {
        return repo.findByEventIdOrderByCreatedAt(eventoId).stream().map(this::paraDominio).toList();
    }

    @Override
    public void apagar(UUID id) {
        repo.deleteById(id);
    }

    @Override
    public long apagarPorEvento(UUID eventoId) {
        return repo.deleteByEventId(eventoId);
    }

    private Convidado paraDominio(ConvidadoJpa j) {
        return new Convidado(j.getId(), j.getEventId(),
                new NomeCriptografado(j.getFullNameEncrypted()),
                new NomeCriptografado(j.getPhoneEncrypted()),
                j.getPhoneHash(), j.getCompanionsAllowed(), j.getCreatedAt());
    }
}
