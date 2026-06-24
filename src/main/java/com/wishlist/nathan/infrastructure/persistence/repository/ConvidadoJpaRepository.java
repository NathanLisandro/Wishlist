package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.infrastructure.persistence.entity.ConvidadoJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConvidadoJpaRepository extends JpaRepository<ConvidadoJpa, UUID> {

    Optional<ConvidadoJpa> findByEventIdAndPhoneHash(UUID eventId, byte[] phoneHash);

    long countByEventId(UUID eventId);

    List<ConvidadoJpa> findByEventIdOrderByCreatedAt(UUID eventId);

    Page<ConvidadoJpa> findByEventId(UUID eventId, Pageable pageable);

    long deleteByEventId(UUID eventId);
}
