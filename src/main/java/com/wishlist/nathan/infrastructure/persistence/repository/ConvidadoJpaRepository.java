package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.infrastructure.persistence.entity.ConvidadoJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConvidadoJpaRepository extends JpaRepository<ConvidadoJpa, UUID> {

    Optional<ConvidadoJpa> findByEventIdAndPhoneHash(UUID eventId, byte[] phoneHash);

    long countByEventId(UUID eventId);

    List<ConvidadoJpa> findByEventIdOrderByCreatedAt(UUID eventId);

    long deleteByEventId(UUID eventId);
}
