package com.wishlist.nathan.infrastructure.persistence.adapter;

import com.wishlist.nathan.application.port.ImportJobRepositorioPort;
import com.wishlist.nathan.infrastructure.persistence.entity.ImportJobJpa;
import com.wishlist.nathan.infrastructure.persistence.repository.ImportJobJpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ImportJobRepositorioAdapter implements ImportJobRepositorioPort {

    private final ImportJobJpaRepository repo;

    public ImportJobRepositorioAdapter(ImportJobJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public UUID registrar(Registro r) {
        ImportJobJpa jpa = new ImportJobJpa();
        jpa.setId(UUID.randomUUID());
        jpa.setEventId(r.eventoId());
        jpa.setType(r.tipo());
        jpa.setFilename(r.arquivo());
        jpa.setStatus(r.status());
        jpa.setRowsTotal(r.total());
        jpa.setRowsOk(r.ok());
        jpa.setRowsFailed(r.falhas());
        jpa.setErrorReport(r.relatorioErrosJson());
        return repo.save(jpa).getId();
    }
}
