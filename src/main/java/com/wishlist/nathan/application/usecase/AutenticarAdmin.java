package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.CredenciaisInvalidasException;
import com.wishlist.nathan.application.port.ContaRepositorioPort;
import com.wishlist.nathan.application.port.SenhaPort;
import com.wishlist.nathan.application.port.TokenSessaoPort;
import com.wishlist.nathan.domain.comum.Email;
import com.wishlist.nathan.domain.conta.Conta;
import org.springframework.stereotype.Service;

/** Autentica o admin e emite um token de sessão. */
@Service
public class AutenticarAdmin {

    private final ContaRepositorioPort contas;
    private final SenhaPort senhas;
    private final TokenSessaoPort tokens;

    public AutenticarAdmin(ContaRepositorioPort contas, SenhaPort senhas, TokenSessaoPort tokens) {
        this.contas = contas;
        this.senhas = senhas;
        this.tokens = tokens;
    }

    public Resultado executar(Comando cmd) {
        Conta conta = contas.buscarPorEmail(new Email(cmd.email()))
                .orElseThrow(() -> new CredenciaisInvalidasException("Credenciais inválidas"));
        if (!conta.ativa() || !senhas.confere(cmd.senha(), conta.getHashSenha())) {
            throw new CredenciaisInvalidasException("Credenciais inválidas");
        }
        return new Resultado(tokens.emitirTokenAdmin(conta.getId()), conta.getNomeExibicao());
    }

    public record Comando(String email, String senha) {}

    public record Resultado(String token, String nomeExibicao) {}
}
