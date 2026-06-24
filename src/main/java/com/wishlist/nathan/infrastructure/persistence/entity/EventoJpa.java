package com.wishlist.nathan.infrastructure.persistence.entity;

import com.wishlist.nathan.domain.evento.StatusEvento;
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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "event")
@Getter
@Setter
@NoArgsConstructor
public class EventoJpa {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "category_id", nullable = false)
    private Short categoryId;

    @Column(nullable = false)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "rsvp_enabled", nullable = false)
    private boolean rsvpEnabled;

    @Column(name = "rsvp_deadline")
    private Instant rsvpDeadline;

    @Column(name = "money_gift_enabled", nullable = false)
    private boolean moneyGiftEnabled;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "event_status")
    private StatusEvento status;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column
    private String location;

    @Column(name = "couple_message")
    private String coupleMessage;

    @Column(name = "closing_message")
    private String closingMessage;

    @Column(name = "thank_you_message")
    private String thankYouMessage;

    @Column(name = "manage_invite_token")
    private String manageInviteToken;

    @Column(name = "theme_palette")
    private String themePalette;

    @Column(name = "accent_color")
    private String accentColor;

    @Column(name = "background_url")
    private String backgroundUrl;

    @Column(name = "background_overlay")
    private Short backgroundOverlay;

    @Column(name = "animation_style")
    private String animationStyle;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
