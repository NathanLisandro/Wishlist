package com.wishlist.nathan.infrastructure.persistence.adapter;

import com.wishlist.nathan.application.port.ContaRepositorioPort;
import com.wishlist.nathan.domain.comum.Email;
import com.wishlist.nathan.domain.conta.Conta;
import com.wishlist.nathan.infrastructure.persistence.entity.ContaJpa;
import com.wishlist.nathan.infrastructure.persistence.repository.ContaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ContaRepositorioAdapter implements ContaRepositorioPort {

    private final ContaJpaRepository repo;

    public ContaRepositorioAdapter(ContaJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Conta salvar(Conta conta) {
        ContaJpa jpa = new ContaJpa();
        jpa.setId(conta.getId() != null ? conta.getId() : UUID.randomUUID());
        jpa.setEmail(conta.getEmail().valor());
        jpa.setPasswordHash(conta.getHashSenha());
        jpa.setDisplayName(conta.getNomeExibicao());
        jpa.setEmailVerified(conta.isEmailVerificado());
        jpa.setStatus(conta.getStatus());
        return paraDominio(repo.save(jpa));
    }

    @Override
    public Optional<Conta> buscarPorId(UUID id) {
        return repo.findById(id).map(this::paraDominio);
    }

    @Override
    public Optional<Conta> buscarPorEmail(Email email) {
        return repo.findByEmail(email.valor()).map(this::paraDominio);
    }

    @Override
    public boolean existePorEmail(Email email) {
        return repo.existsByEmail(email.valor());
    }

    private Conta paraDominio(ContaJpa j) {
        return new Conta(j.getId(), new Email(j.getEmail()), j.getPasswordHash(),
                j.getDisplayName(), j.isEmailVerified(), j.getStatus(), j.getCreatedAt());
    }
}
