package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.domain.pagamento.StatusPresenteDinheiro;
import com.wishlist.nathan.infrastructure.persistence.entity.MoneyGiftJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface MoneyGiftJpaRepository extends JpaRepository<MoneyGiftJpa, UUID> {

    Optional<MoneyGiftJpa> findByMpPaymentId(String mpPaymentId);

    long countByEventIdAndStatus(UUID eventId, StatusPresenteDinheiro status);

    @Query("select coalesce(sum(m.amount), 0) from MoneyGiftJpa m "
            + "where m.eventId = :eventId and m.status = :status")
    BigDecimal somar(@Param("eventId") UUID eventId, @Param("status") StatusPresenteDinheiro status);
}
