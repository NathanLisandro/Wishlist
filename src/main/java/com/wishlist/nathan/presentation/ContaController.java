package com.wishlist.nathan.presentation;

import com.wishlist.nathan.application.dto.SessaoAdmin;
import com.wishlist.nathan.application.port.ConfigMercadoPagoRepositorioPort;
import com.wishlist.nathan.application.usecase.ConfigurarMercadoPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/account/payment-config")
public class ContaController {

    private final ConfigurarMercadoPago configurar;
    private final ConfigMercadoPagoRepositorioPort configs;

    public ContaController(ConfigurarMercadoPago configurar, ConfigMercadoPagoRepositorioPort configs) {
        this.configurar = configurar;
        this.configs = configs;
    }

    @PutMapping
    public ResponseEntity<Void> salvar(@AuthenticationPrincipal SessaoAdmin sessao,
                                       @Valid @RequestBody ConfigRequest req) {
        configurar.executar(sessao.contaId(), new ConfigurarMercadoPago.Comando(
                req.accessToken(), req.publicKey(), req.webhookSecret()));
        return ResponseEntity.noContent().build();
    }

    /** Status da configuração — nunca expõe o access token. */
    @GetMapping
    public Map<String, Object> consultar(@AuthenticationPrincipal SessaoAdmin sessao) {
        return configs.buscarPorConta(sessao.contaId())
                .map(c -> Map.<String, Object>of("configurado", true,
                        "publicKey", c.getPublicKey() == null ? "" : c.getPublicKey()))
                .orElse(Map.of("configurado", false));
    }

    public record ConfigRequest(@NotBlank String accessToken, String publicKey, String webhookSecret) {}
}
