package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.infrastructure.persistence.entity.ColaboradorEventoJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColaboradorEventoJpaRepository extends JpaRepository<ColaboradorEventoJpa, UUID> {

    boolean existsByEventIdAndAccountId(UUID eventId, UUID accountId);

    Optional<ColaboradorEventoJpa> findByEventIdAndAccountId(UUID eventId, UUID accountId);

    void deleteByEventIdAndAccountId(UUID eventId, UUID accountId);

    List<ColaboradorEventoJpa> findByAccountId(UUID accountId);

    List<ColaboradorEventoJpa> findByEventId(UUID eventId);
}
