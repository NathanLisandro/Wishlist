package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.port.ConfigMercadoPagoRepositorioPort;
import com.wishlist.nathan.application.port.CriptografiaPort;
import com.wishlist.nathan.domain.comum.TokenCriptografado;
import com.wishlist.nathan.domain.conta.ConfigMercadoPago;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Salva as credenciais do Mercado Pago da conta (cifradas em repouso). */
@Service
public class ConfigurarMercadoPago {

    private final ConfigMercadoPagoRepositorioPort configs;
    private final CriptografiaPort cripto;

    public ConfigurarMercadoPago(ConfigMercadoPagoRepositorioPort configs, CriptografiaPort cripto) {
        this.configs = configs;
        this.cripto = cripto;
    }

    @Transactional
    public void executar(UUID contaId, Comando cmd) {
        if (cmd.accessToken() == null || cmd.accessToken().isBlank()) {
            throw new IllegalArgumentException("accessToken é obrigatório");
        }
        TokenCriptografado token = new TokenCriptografado(cripto.criptografar(cmd.accessToken()));
        TokenCriptografado webhook = (cmd.webhookSecret() != null && !cmd.webhookSecret().isBlank())
                ? new TokenCriptografado(cripto.criptografar(cmd.webhookSecret()))
                : null;
        configs.salvarOuAtualizar(new ConfigMercadoPago(null, contaId, token, cmd.publicKey(), webhook));
    }

    public record Comando(String accessToken, String publicKey, String webhookSecret) {}
}
