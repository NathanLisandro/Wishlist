package com.wishlist.nathan.infrastructure.persistence.entity;

import com.wishlist.nathan.domain.importacao.StatusImportacao;
import com.wishlist.nathan.domain.importacao.TipoImportacao;
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
@Table(name = "import_job")
@Getter
@Setter
@NoArgsConstructor
public class ImportJobJpa {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "import_type")
    private TipoImportacao type;

    @Column(nullable = false)
    private String filename;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "import_status")
    private StatusImportacao status;

    @Column(name = "rows_total", nullable = false)
    private int rowsTotal;

    @Column(name = "rows_ok", nullable = false)
    private int rowsOk;

    @Column(name = "rows_failed", nullable = false)
    private int rowsFailed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "error_report", columnDefinition = "jsonb")
    private String errorReport;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
