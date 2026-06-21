package com.wishlist.nathan.presentation;

import com.wishlist.nathan.application.dto.SessaoConvidado;
import com.wishlist.nathan.application.usecase.CancelarPresenca;
import com.wishlist.nathan.application.usecase.CancelarPresente;
import com.wishlist.nathan.application.usecase.ConfirmarPresenca;
import com.wishlist.nathan.application.usecase.ConsultarEventoPublico;
import com.wishlist.nathan.application.usecase.ConsultarRsvp;
import com.wishlist.nathan.application.usecase.CriarPresenteDinheiro;
import com.wishlist.nathan.application.usecase.EscolherPresente;
import com.wishlist.nathan.application.usecase.IniciarSessaoConvidado;
import com.wishlist.nathan.application.usecase.ListarPresentes;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Endpoints públicos do convidado (sessão por telefone, presentes e RSVP). */
@RestController
@RequestMapping("/public/events/{slug}")
public class PublicoController {

    private final IniciarSessaoConvidado iniciarSessao;
    private final ListarPresentes listarPresentes;
    private final EscolherPresente escolherPresente;
    private final CancelarPresente cancelarPresente;
    private final ConfirmarPresenca confirmarPresenca;
    private final CancelarPresenca cancelarPresenca;
    private final CriarPresenteDinheiro criarPresenteDinheiro;
    private final ConsultarEventoPublico consultarEventoPublico;
    private final ConsultarRsvp consultarRsvp;

    public PublicoController(IniciarSessaoConvidado iniciarSessao, ListarPresentes listarPresentes,
                            EscolherPresente escolherPresente, CancelarPresente cancelarPresente,
                            ConfirmarPresenca confirmarPresenca, CancelarPresenca cancelarPresenca,
                            CriarPresenteDinheiro criarPresenteDinheiro,
                            ConsultarEventoPublico consultarEventoPublico, ConsultarRsvp consultarRsvp) {
        this.iniciarSessao = iniciarSessao;
        this.listarPresentes = listarPresentes;
        this.escolherPresente = escolherPresente;
        this.cancelarPresente = cancelarPresente;
        this.confirmarPresenca = confirmarPresenca;
        this.cancelarPresenca = cancelarPresenca;
        this.criarPresenteDinheiro = criarPresenteDinheiro;
        this.consultarEventoPublico = consultarEventoPublico;
        this.consultarRsvp = consultarRsvp;
    }

    @GetMapping
    public ConsultarEventoPublico.Resultado eventoPublico(@PathVariable String slug) {
        return consultarEventoPublico.executar(slug);
    }

    @PostMapping("/guest-session")
    public IniciarSessaoConvidado.Resultado abrirSessao(@PathVariable String slug,
                                                        @Valid @RequestBody SessaoRequest req,
                                                        HttpServletRequest http) {
        return iniciarSessao.executar(new IniciarSessaoConvidado.Comando(
                slug, req.telefone(), ip(http), userAgent(http)));
    }

    @GetMapping("/gifts")
    public List<ListarPresentes.ItemDisponivel> listar(@AuthenticationPrincipal SessaoConvidado sessao) {
        return listarPresentes.executar(sessao.eventoId(), sessao.convidadoId());
    }

    @GetMapping("/my-gifts")
    public List<ListarPresentes.ItemDisponivel> meusPresentes(@AuthenticationPrincipal SessaoConvidado sessao) {
        return listarPresentes.meusPresentes(sessao.eventoId(), sessao.convidadoId());
    }

    @PostMapping("/gifts/{giftId}/claim")
    public EscolherPresente.Resultado reservar(@AuthenticationPrincipal SessaoConvidado sessao,
                                               @PathVariable UUID giftId,
                                               @RequestHeader("Idempotency-Key") String idempotencyKey,
                                               HttpServletRequest http) {
        return escolherPresente.executar(new EscolherPresente.Comando(
                sessao.eventoId(), sessao.convidadoId(), giftId, idempotencyKey, ip(http), userAgent(http)));
    }

    @DeleteMapping("/gifts/{giftId}/claim")
    public ResponseEntity<Void> cancelarReserva(@AuthenticationPrincipal SessaoConvidado sessao,
                                                @PathVariable UUID giftId, HttpServletRequest http) {
        cancelarPresente.executar(new CancelarPresente.Comando(
                sessao.eventoId(), sessao.convidadoId(), giftId, ip(http), userAgent(http)));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rsvp")
    public ConsultarRsvp.Resultado rsvpAtual(@AuthenticationPrincipal SessaoConvidado sessao) {
        return consultarRsvp.executar(sessao.eventoId(), sessao.convidadoId());
    }

    @PostMapping("/rsvp")
    public Map<String, Integer> confirmar(@AuthenticationPrincipal SessaoConvidado sessao,
                                          @Valid @RequestBody RsvpRequest req, HttpServletRequest http) {
        int total = confirmarPresenca.executar(new ConfirmarPresenca.Comando(
                sessao.eventoId(), sessao.convidadoId(),
                req.acompanhantes() == null ? List.of() : req.acompanhantes(), ip(http), userAgent(http)));
        return Map.of("totalPessoas", total);
    }

    @DeleteMapping("/rsvp")
    public ResponseEntity<Void> cancelarRsvp(@AuthenticationPrincipal SessaoConvidado sessao,
                                             HttpServletRequest http) {
        cancelarPresenca.executar(new CancelarPresenca.Comando(
                sessao.eventoId(), sessao.convidadoId(), ip(http), userAgent(http)));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/money-gift")
    public CriarPresenteDinheiro.Resultado presenteEmDinheiro(@AuthenticationPrincipal SessaoConvidado sessao,
                                                              @Valid @RequestBody MoneyGiftRequest req,
                                                              HttpServletRequest http) {
        return criarPresenteDinheiro.executar(new CriarPresenteDinheiro.Comando(
                sessao.eventoId(), sessao.convidadoId(), req.valor(), req.mensagem(), ip(http), userAgent(http)));
    }

    private static String ip(HttpServletRequest http) {
        return http.getRemoteAddr();
    }

    private static String userAgent(HttpServletRequest http) {
        return http.getHeader("User-Agent");
    }

    public record SessaoRequest(@NotBlank String telefone) {}

    public record RsvpRequest(List<String> acompanhantes) {}

    public record MoneyGiftRequest(@NotNull @Positive BigDecimal valor, String mensagem) {}
}
