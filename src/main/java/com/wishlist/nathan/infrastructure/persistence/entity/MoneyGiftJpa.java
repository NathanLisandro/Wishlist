package com.wishlist.nathan.infrastructure.persistence.entity;

import com.wishlist.nathan.domain.pagamento.StatusPresenteDinheiro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "money_gift")
@Getter
@Setter
@NoArgsConstructor
public class MoneyGiftJpa {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "guest_id")
    private UUID guestId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column
    private String message;

    @Column(name = "mp_preference_id")
    private String mpPreferenceId;

    @Column(name = "mp_payment_id", unique = true)
    private String mpPaymentId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "money_gift_status")
    private StatusPresenteDinheiro status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
