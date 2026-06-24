package com.wishlist.nathan.infrastructure.persistence.entity;

import com.wishlist.nathan.domain.presente.StatusPresente;
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
@Table(name = "gift_item")
@Getter
@Setter
@NoArgsConstructor
public class ItemPresenteJpa {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private String name;

    @Column
    private String variant;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Column(name = "product_link")
    private String productLink;

    @Column(name = "image_url")
    private String imageUrl;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "gift_status")
    private StatusPresente status;

    @Column(name = "natural_key", nullable = false)
    private String naturalKey;

    @Column(name = "estimated_price")
    private BigDecimal estimatedPrice;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
