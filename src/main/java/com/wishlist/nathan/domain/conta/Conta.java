package com.wishlist.nathan.domain.conta;

import com.wishlist.nathan.domain.comum.Email;

import java.time.Instant;
import java.util.UUID;

/**
 * Conta = tenant (o casal). Raiz de isolamento multi-tenant: todo dado do sistema
 * pertence, direta ou indiretamente, a uma conta.
 */
public class Conta {

    private final UUID id;
    private final Email email;
    private String hashSenha;
    private String nomeExibicao;
    private boolean emailVerificado;
    private StatusConta status;
    private boolean onboardingConcluido;
    private final Instant criadoEm;

    public Conta(UUID id, Email email, String hashSenha, String nomeExibicao,
                 boolean emailVerificado, StatusConta status, boolean onboardingConcluido,
                 Instant criadoEm) {
        this.id = id;
        this.email = exigir(email, "email");
        this.hashSenha = exigir(hashSenha, "hashSenha");
        this.nomeExibicao = exigir(nomeExibicao, "nomeExibicao");
        this.emailVerificado = emailVerificado;
        this.status = status == null ? StatusConta.ACTIVE : status;
        this.onboardingConcluido = onboardingConcluido;
        this.criadoEm = criadoEm;
    }

    /** Cria uma nova conta (ainda não persistida) em estado padrão: onboarding pendente. */
    public static Conta nova(Email email, String hashSenha, String nomeExibicao) {
        return new Conta(null, email, hashSenha, nomeExibicao, false, StatusConta.ACTIVE, false, null);
    }

    public void verificarEmail() {
        this.emailVerificado = true;
    }

    /** Marca o tour de boas-vindas como concluído — não volta a ser exibido. */
    public void concluirOnboarding() {
        this.onboardingConcluido = true;
    }

    public void suspender() {
        this.status = StatusConta.SUSPENDED;
    }

    public boolean ativa() {
        return status == StatusConta.ACTIVE;
    }

    private static <T> T exigir(T valor, String campo) {
        if (valor == null || (valor instanceof String s && s.isBlank())) {
            throw new IllegalArgumentException("Campo obrigatório: " + campo);
        }
        return valor;
    }

    public UUID getId() { return id; }
    public Email getEmail() { return email; }
    public String getHashSenha() { return hashSenha; }
    public String getNomeExibicao() { return nomeExibicao; }
    public boolean isEmailVerificado() { return emailVerificado; }
    public StatusConta getStatus() { return status; }
    public boolean isOnboardingConcluido() { return onboardingConcluido; }
    public Instant getCriadoEm() { return criadoEm; }
}
