package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.infrastructure.persistence.entity.CategoriaEventoJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaEventoJpaRepository extends JpaRepository<CategoriaEventoJpa, Short> {

    List<CategoriaEventoJpa> findByActiveTrue();
}
