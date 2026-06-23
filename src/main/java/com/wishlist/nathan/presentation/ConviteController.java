package com.wishlist.nathan.presentation;

import com.wishlist.nathan.application.dto.SessaoAdmin;
import com.wishlist.nathan.application.usecase.AceitarConviteGestao;
import com.wishlist.nathan.application.usecase.ConsultarConvite;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/** Convite de co-gestão: prévia pública (pelo token) + aceite (conta autenticada). */
@RestController
@RequestMapping("/manage-invites")
public class ConviteController {

    private final ConsultarConvite consultarConvite;
    private final AceitarConviteGestao aceitarConvite;

    public ConviteController(ConsultarConvite consultarConvite, AceitarConviteGestao aceitarConvite) {
        this.consultarConvite = consultarConvite;
        this.aceitarConvite = aceitarConvite;
    }

    /** Público: mostra de qual evento é o convite (o token é o segredo). */
    @GetMapping("/{token}")
    public ConsultarConvite.Resultado preview(@PathVariable String token) {
        return consultarConvite.executar(token);
    }

    /** Admin autenticado aceita e passa a co-gerenciar; devolve o id do evento. */
    @PostMapping("/{token}/accept")
    public Map<String, UUID> aceitar(@AuthenticationPrincipal SessaoAdmin sessao,
                                     @PathVariable String token) {
        UUID eventoId = aceitarConvite.executar(sessao.contaId(), token);
        return Map.of("eventoId", eventoId);
    }
}
