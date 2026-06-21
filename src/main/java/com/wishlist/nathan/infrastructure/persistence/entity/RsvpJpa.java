package com.wishlist.nathan.infrastructure.persistence.entity;

import com.wishlist.nathan.domain.rsvp.StatusRsvp;
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
@Table(name = "rsvp")
@Getter
@Setter
@NoArgsConstructor
public class RsvpJpa {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "guest_id", nullable = false)
    private UUID guestId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "rsvp_status")
    private StatusRsvp status;

    @Column(name = "responded_at", insertable = false, updatable = false)
    private Instant respondedAt;
}
