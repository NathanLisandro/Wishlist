package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.infrastructure.persistence.entity.ConfigMercadoPagoJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConfigMercadoPagoJpaRepository extends JpaRepository<ConfigMercadoPagoJpa, UUID> {

    Optional<ConfigMercadoPagoJpa> findByAccountId(UUID accountId);
}
