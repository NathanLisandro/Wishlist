package com.wishlist.nathan.presentation;

import com.wishlist.nathan.application.dto.SessaoAdmin;
import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ContaRepositorioPort;
import com.wishlist.nathan.domain.conta.Conta;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Dados da conta autenticada (admin). */
@RestController
@RequestMapping("/account")
public class AccountController {

    private final ContaRepositorioPort contas;

    public AccountController(ContaRepositorioPort contas) {
        this.contas = contas;
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal SessaoAdmin sessao) {
        var conta = carregar(sessao);
        return new MeResponse(conta.getId(), conta.getEmail().valor(),
                conta.getNomeExibicao(), conta.isOnboardingConcluido());
    }

    /** Marca o tour de boas-vindas como concluído (idempotente). */
    @PostMapping("/onboarding/concluir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void concluirOnboarding(@AuthenticationPrincipal SessaoAdmin sessao) {
        var conta = carregar(sessao);
        if (!conta.isOnboardingConcluido()) {
            conta.concluirOnboarding();
            contas.salvar(conta);
        }
    }

    private Conta carregar(SessaoAdmin sessao) {
        return contas.buscarPorId(sessao.contaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada"));
    }

    public record MeResponse(UUID id, String email, String nomeExibicao, boolean onboardingConcluido) {}
}
