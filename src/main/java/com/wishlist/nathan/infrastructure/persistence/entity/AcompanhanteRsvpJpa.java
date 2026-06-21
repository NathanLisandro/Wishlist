package com.wishlist.nathan.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rsvp_companion")
@Getter
@Setter
@NoArgsConstructor
public class AcompanhanteRsvpJpa {

    @Id
    private UUID id;

    @Column(name = "rsvp_id", nullable = false)
    private UUID rsvpId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
