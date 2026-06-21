package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.infrastructure.persistence.entity.EventoJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventoJpaRepository extends JpaRepository<EventoJpa, UUID> {

    Optional<EventoJpa> findFirstBySlug(String slug);

    List<EventoJpa> findByAccountId(UUID accountId);

    boolean existsByAccountIdAndSlug(UUID accountId, String slug);
}
