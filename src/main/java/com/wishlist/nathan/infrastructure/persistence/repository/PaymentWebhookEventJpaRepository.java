package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.infrastructure.persistence.entity.PaymentWebhookEventJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentWebhookEventJpaRepository extends JpaRepository<PaymentWebhookEventJpa, UUID> {

    boolean existsByMpEventId(String mpEventId);
}
