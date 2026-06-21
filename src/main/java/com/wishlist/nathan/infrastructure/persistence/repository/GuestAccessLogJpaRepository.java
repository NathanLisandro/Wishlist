package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.infrastructure.persistence.entity.GuestAccessLogJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GuestAccessLogJpaRepository extends JpaRepository<GuestAccessLogJpa, UUID> {

    List<GuestAccessLogJpa> findTop50ByEventIdOrderByCreatedAtDesc(UUID eventId);
}
