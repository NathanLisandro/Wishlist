package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.domain.presente.StatusPresente;
import com.wishlist.nathan.infrastructure.persistence.entity.ItemPresenteJpa;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemPresenteJpaRepository extends JpaRepository<ItemPresenteJpa, UUID> {

    List<ItemPresenteJpa> findByEventIdAndStatus(UUID eventId, StatusPresente status);

    List<ItemPresenteJpa> findByEventIdAndStatusNotOrderByCreatedAt(UUID eventId, StatusPresente status);

    Page<ItemPresenteJpa> findByEventIdAndStatusNot(UUID eventId, StatusPresente status, Pageable pageable);

    /** Busca do painel por nome/variante (case-insensitive; :busca já vem com % e escapado). */
    @Query("""
            select i from ItemPresenteJpa i
            where i.eventId = :eventId and i.status <> :status
              and (lower(i.name) like lower(:busca) escape '\\'
                   or lower(i.variant) like lower(:busca) escape '\\')
            """)
    Page<ItemPresenteJpa> buscarPorEventoETexto(@Param("eventId") UUID eventId,
                                                @Param("status") StatusPresente status,
                                                @Param("busca") String busca,
                                                Pageable pageable);

    long deleteByEventId(UUID eventId);

    /** Lock pessimista (SELECT ... FOR UPDATE) para a reserva sem oversell. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from ItemPresenteJpa i where i.id = :id")
    Optional<ItemPresenteJpa> buscarComLock(@Param("id") UUID id);
}
