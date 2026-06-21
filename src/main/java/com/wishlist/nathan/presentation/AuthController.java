package com.wishlist.nathan.presentation;

import com.wishlist.nathan.application.usecase.AutenticarAdmin;
import com.wishlist.nathan.application.usecase.RegistrarConta;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegistrarConta registrarConta;
    private final AutenticarAdmin autenticarAdmin;

    public AuthController(RegistrarConta registrarConta, AutenticarAdmin autenticarAdmin) {
        this.registrarConta = registrarConta;
        this.autenticarAdmin = autenticarAdmin;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, UUID>> registrar(@Valid @RequestBody RegistroRequest req) {
        UUID id = registrarConta.executar(new RegistrarConta.Comando(req.email(), req.senha(), req.nomeExibicao()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        var r = autenticarAdmin.executar(new AutenticarAdmin.Comando(req.email(), req.senha()));
        return new LoginResponse(r.token(), r.nomeExibicao());
    }

    public record RegistroRequest(@Email @NotBlank String email, @NotBlank String senha,
                                  @NotBlank String nomeExibicao) {}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String senha) {}

    public record LoginResponse(String token, String nomeExibicao) {}
}
