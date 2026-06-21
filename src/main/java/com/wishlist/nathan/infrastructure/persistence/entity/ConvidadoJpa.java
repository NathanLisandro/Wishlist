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
@Table(name = "guest")
@Getter
@Setter
@NoArgsConstructor
public class ConvidadoJpa {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "full_name_encrypted", nullable = false)
    private byte[] fullNameEncrypted;

    @Column(name = "phone_encrypted", nullable = false)
    private byte[] phoneEncrypted;

    @Column(name = "phone_hash", nullable = false)
    private byte[] phoneHash;

    @Column(name = "companions_allowed", nullable = false)
    private short companionsAllowed;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
