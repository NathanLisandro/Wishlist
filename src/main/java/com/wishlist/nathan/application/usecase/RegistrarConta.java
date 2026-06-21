package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.ConflitoException;
import com.wishlist.nathan.application.port.ContaRepositorioPort;
import com.wishlist.nathan.application.port.SenhaPort;
import com.wishlist.nathan.domain.comum.Email;
import com.wishlist.nathan.domain.conta.Conta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Cria uma conta (tenant = casal). */
@Service
public class RegistrarConta {

    private final ContaRepositorioPort contas;
    private final SenhaPort senhas;

    public RegistrarConta(ContaRepositorioPort contas, SenhaPort senhas) {
        this.contas = contas;
        this.senhas = senhas;
    }

    @Transactional
    public UUID executar(Comando cmd) {
        Email email = new Email(cmd.email());
        if (contas.existePorEmail(email)) {
            throw new ConflitoException("E-mail já cadastrado");
        }
        if (cmd.senha() == null || cmd.senha().length() < 8) {
            throw new IllegalArgumentException("Senha deve ter ao menos 8 caracteres");
        }
        Conta conta = Conta.nova(email, senhas.codificar(cmd.senha()), cmd.nomeExibicao());
        return contas.salvar(conta).getId();
    }

    public record Comando(String email, String senha, String nomeExibicao) {}
}
