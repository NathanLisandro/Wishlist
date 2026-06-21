package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.infrastructure.persistence.entity.ImportJobJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImportJobJpaRepository extends JpaRepository<ImportJobJpa, UUID> {
}
