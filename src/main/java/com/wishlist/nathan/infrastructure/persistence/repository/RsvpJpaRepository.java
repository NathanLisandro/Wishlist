package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.domain.rsvp.StatusRsvp;
import com.wishlist.nathan.infrastructure.persistence.entity.RsvpJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RsvpJpaRepository extends JpaRepository<RsvpJpa, UUID> {

    Optional<RsvpJpa> findByEventIdAndGuestId(UUID eventId, UUID guestId);

    long countByEventIdAndStatus(UUID eventId, StatusRsvp status);
}
