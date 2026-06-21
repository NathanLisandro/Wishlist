package com.wishlist.nathan.presentation;

import com.wishlist.nathan.application.dto.SessaoAdmin;
import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ContaRepositorioPort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
        var conta = contas.buscarPorId(sessao.contaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada"));
        return new MeResponse(conta.getId(), conta.getEmail().valor(), conta.getNomeExibicao());
    }

    public record MeResponse(UUID id, String email, String nomeExibicao) {}
}
