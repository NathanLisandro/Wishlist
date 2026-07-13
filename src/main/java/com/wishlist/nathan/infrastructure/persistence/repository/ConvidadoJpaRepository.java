package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.domain.rsvp.StatusRsvp;
import com.wishlist.nathan.infrastructure.persistence.entity.ConvidadoJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConvidadoJpaRepository extends JpaRepository<ConvidadoJpa, UUID> {

    Optional<ConvidadoJpa> findByEventIdAndPhoneHash(UUID eventId, byte[] phoneHash);

    long countByEventId(UUID eventId);

    List<ConvidadoJpa> findByEventIdOrderByCreatedAt(UUID eventId);

    Page<ConvidadoJpa> findByEventId(UUID eventId, Pageable pageable);

    /** Convidados cujo RSVP está em um dos status dados (ex.: confirmados). */
    @Query("""
            select c from ConvidadoJpa c
            where c.eventId = :eventId
              and exists (select r.id from RsvpJpa r
                          where r.eventId = c.eventId and r.guestId = c.id and r.status in :statuses)
            """)
    Page<ConvidadoJpa> buscarComRsvpEm(@Param("eventId") UUID eventId,
                                       @Param("statuses") Collection<StatusRsvp> statuses,
                                       Pageable pageable);

    /** Convidados sem nenhum RSVP — os "pendentes" do painel. */
    @Query("""
            select c from ConvidadoJpa c
            where c.eventId = :eventId
              and not exists (select r.id from RsvpJpa r
                              where r.eventId = c.eventId and r.guestId = c.id)
            """)
    Page<ConvidadoJpa> buscarSemRsvp(@Param("eventId") UUID eventId, Pageable pageable);

    long deleteByEventId(UUID eventId);
}
