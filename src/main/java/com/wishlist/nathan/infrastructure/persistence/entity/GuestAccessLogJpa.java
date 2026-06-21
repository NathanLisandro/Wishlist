package com.wishlist.nathan.infrastructure.persistence.entity;

import com.wishlist.nathan.domain.convidado.AcaoAcessoConvidado;
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
@Table(name = "guest_access_log")
@Getter
@Setter
@NoArgsConstructor
public class GuestAccessLogJpa {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "guest_id")
    private UUID guestId;

    @Column(name = "phone_hash")
    private byte[] phoneHash;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "guest_access_action")
    private AcaoAcessoConvidado action;

    @Column
    private String ip;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
