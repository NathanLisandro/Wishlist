package com.wishlist.nathan.application.port;

import com.wishlist.nathan.domain.conta.ConfigMercadoPago;

import java.util.Optional;
import java.util.UUID;

public interface ConfigMercadoPagoRepositorioPort {

    ConfigMercadoPago salvarOuAtualizar(ConfigMercadoPago config);

    Optional<ConfigMercadoPago> buscarPorConta(UUID contaId);
}
