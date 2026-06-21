package com.wishlist.nathan.presentation;

import com.wishlist.nathan.application.dto.SessaoAdmin;
import com.wishlist.nathan.application.usecase.ConsultarDashboard;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/events/{eventoId}/dashboard")
public class DashboardController {

    private final ConsultarDashboard consultarDashboard;

    public DashboardController(ConsultarDashboard consultarDashboard) {
        this.consultarDashboard = consultarDashboard;
    }

    @GetMapping
    public ConsultarDashboard.Resultado consultar(@AuthenticationPrincipal SessaoAdmin sessao,
                                                  @PathVariable UUID eventoId) {
        return consultarDashboard.executar(sessao.contaId(), eventoId);
    }
}
