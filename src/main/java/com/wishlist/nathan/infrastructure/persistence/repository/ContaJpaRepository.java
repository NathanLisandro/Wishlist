package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.infrastructure.persistence.entity.ContaJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContaJpaRepository extends JpaRepository<ContaJpa, UUID> {

    Optional<ContaJpa> findByEmail(String email);

    boolean existsByEmail(String email);
}
