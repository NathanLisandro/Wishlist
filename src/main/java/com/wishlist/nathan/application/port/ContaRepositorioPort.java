package com.wishlist.nathan.application.port;

import com.wishlist.nathan.domain.comum.Email;
import com.wishlist.nathan.domain.conta.Conta;

import java.util.Optional;
import java.util.UUID;

public interface ContaRepositorioPort {

    Conta salvar(Conta conta);

    Optional<Conta> buscarPorId(UUID id);

    Optional<Conta> buscarPorEmail(Email email);

    boolean existePorEmail(Email email);
}
