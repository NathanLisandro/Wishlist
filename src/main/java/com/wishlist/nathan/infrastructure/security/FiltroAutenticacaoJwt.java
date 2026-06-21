package com.wishlist.nathan.infrastructure.security;

import com.wishlist.nathan.application.port.TokenSessaoPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lê o token Bearer e popula o SecurityContext. Tenta primeiro como token de
 * admin; se não for, tenta como token de convidado. Sem token válido a requisição
 * segue anônima (as regras de autorização decidem o acesso).
 */
@Component
public class FiltroAutenticacaoJwt extends OncePerRequestFilter {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_CONVIDADO = "ROLE_GUEST";

    private final TokenSessaoPort tokens;

    public FiltroAutenticacaoJwt(TokenSessaoPort tokens) {
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String token = extrairToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            autenticar(token, request);
        }
        chain.doFilter(request, response);
    }

    private void autenticar(String token, HttpServletRequest request) {
        try {
            var sessao = tokens.validarAdmin(token);
            registrar(sessao, ROLE_ADMIN, request);
            return;
        } catch (TokenSessaoPort.SessaoInvalidaException ignorado) {
            // não é admin — tenta convidado
        }
        try {
            var sessao = tokens.validarConvidado(token);
            registrar(sessao, ROLE_CONVIDADO, request);
        } catch (TokenSessaoPort.SessaoInvalidaException ignorado) {
            // token inválido — segue anônimo
        }
    }

    private void registrar(Object principal, String role, HttpServletRequest request) {
        var auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority(role)));
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private String extrairToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
