package com.wishlist.nathan.infrastructure.persistence.adapter;

import com.wishlist.nathan.application.port.RsvpRepositorioPort;
import com.wishlist.nathan.domain.rsvp.AcompanhanteRsvp;
import com.wishlist.nathan.domain.rsvp.Rsvp;
import com.wishlist.nathan.infrastructure.persistence.entity.AcompanhanteRsvpJpa;
import com.wishlist.nathan.infrastructure.persistence.entity.RsvpJpa;
import com.wishlist.nathan.infrastructure.persistence.repository.AcompanhanteRsvpJpaRepository;
import com.wishlist.nathan.infrastructure.persistence.repository.RsvpJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RsvpRepositorioAdapter implements RsvpRepositorioPort {

    private final RsvpJpaRepository repo;
    private final AcompanhanteRsvpJpaRepository acompanhantesRepo;

    public RsvpRepositorioAdapter(RsvpJpaRepository repo, AcompanhanteRsvpJpaRepository acompanhantesRepo) {
        this.repo = repo;
        this.acompanhantesRepo = acompanhantesRepo;
    }

    @Override
    public Rsvp salvar(Rsvp rsvp) {
        // Upsert por (event_id, guest_id): reaproveita a linha existente, se houver.
        RsvpJpa jpa = repo.findByEventIdAndGuestId(rsvp.getEventoId(), rsvp.getConvidadoId())
                .orElseGet(RsvpJpa::new);
        if (jpa.getId() == null) {
            jpa.setId(rsvp.getId() != null ? rsvp.getId() : UUID.randomUUID());
        }
        jpa.setEventId(rsvp.getEventoId());
        jpa.setGuestId(rsvp.getConvidadoId());
        jpa.setStatus(rsvp.getStatus());
        RsvpJpa salvo = repo.save(jpa);

        // Acompanhantes são regravados a cada confirmação (upsert da lista).
        acompanhantesRepo.deleteByRsvpId(salvo.getId());
        for (AcompanhanteRsvp a : rsvp.getAcompanhantes()) {
            AcompanhanteRsvpJpa aj = new AcompanhanteRsvpJpa();
            aj.setId(UUID.randomUUID());
            aj.setRsvpId(salvo.getId());
            aj.setFullName(a.getNomeCompleto());
            acompanhantesRepo.save(aj);
        }
        return paraDominio(salvo);
    }

    @Override
    public Optional<Rsvp> buscarPorEventoEConvidado(UUID eventoId, UUID convidadoId) {
        return repo.findByEventIdAndGuestId(eventoId, convidadoId).map(this::paraDominio);
    }

    @Override
    public long contarPorEventoEStatus(UUID eventoId, com.wishlist.nathan.domain.rsvp.StatusRsvp status) {
        return repo.countByEventIdAndStatus(eventoId, status);
    }

    @Override
    public long contarAcompanhantesConfirmados(UUID eventoId) {
        return acompanhantesRepo.contarConfirmadosNoEvento(
                eventoId, com.wishlist.nathan.domain.rsvp.StatusRsvp.CONFIRMED);
    }

    private Rsvp paraDominio(RsvpJpa j) {
        List<String> nomes = acompanhantesRepo.findByRsvpId(j.getId()).stream()
                .map(AcompanhanteRsvpJpa::getFullName).toList();
        return Rsvp.reconstituir(j.getId(), j.getEventId(), j.getGuestId(), j.getStatus(),
                j.getRespondedAt(), nomes);
    }
}
