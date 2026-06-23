package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.infrastructure.persistence.entity.EventoJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventoJpaRepository extends JpaRepository<EventoJpa, UUID> {

    Optional<EventoJpa> findFirstBySlug(String slug);

    Optional<EventoJpa> findFirstByManageInviteToken(String manageInviteToken);

    List<EventoJpa> findByAccountId(UUID accountId);

    List<EventoJpa> findByIdIn(java.util.Collection<UUID> ids);

    boolean existsByAccountIdAndSlug(UUID accountId, String slug);
}
