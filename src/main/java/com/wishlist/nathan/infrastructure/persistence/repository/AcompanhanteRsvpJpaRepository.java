package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.domain.rsvp.StatusRsvp;
import com.wishlist.nathan.infrastructure.persistence.entity.AcompanhanteRsvpJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AcompanhanteRsvpJpaRepository extends JpaRepository<AcompanhanteRsvpJpa, UUID> {

    List<AcompanhanteRsvpJpa> findByRsvpId(UUID rsvpId);

    void deleteByRsvpId(UUID rsvpId);

    /** Total de acompanhantes confirmados no evento (status passado como parâmetro). */
    @Query("select count(a) from AcompanhanteRsvpJpa a where a.rsvpId in "
            + "(select r.id from RsvpJpa r where r.eventId = :eventoId and r.status = :status)")
    long contarConfirmadosNoEvento(@Param("eventoId") UUID eventoId, @Param("status") StatusRsvp status);
}
