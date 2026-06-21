package com.wishlist.nathan.infrastructure.persistence.entity;

import com.wishlist.nathan.domain.presente.StatusReserva;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "gift_claim")
@Getter
@Setter
@NoArgsConstructor
public class ReservaPresenteJpa {

    @Id
    private UUID id;

    @Column(name = "gift_item_id", nullable = false)
    private UUID giftItemId;

    @Column(name = "guest_id", nullable = false)
    private UUID guestId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "claim_status")
    private StatusReserva status;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;
}
