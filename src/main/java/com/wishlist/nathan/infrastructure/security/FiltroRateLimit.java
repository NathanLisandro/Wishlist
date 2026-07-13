package com.wishlist.nathan.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting por IP no login do admin e no guest-session (mitiga enumeração de
 * telefone e brute force — SDD §7.3). Buckets em memória (suficiente para 1 instância;
 * em cluster trocar por store distribuído).
 */
public class FiltroRateLimit extends OncePerRequestFilter {

    private final int capacidade;
    private final Duration janela;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public FiltroRateLimit(int capacidade, Duration janela) {
        this.capacidade = capacidade;
        this.janela = janela;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        if (deveLimitar(request)) {
            String chave = request.getRemoteAddr() + "|" + request.getRequestURI();
            Bucket bucket = buckets.computeIfAbsent(chave, k -> novoBucket());
            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                response.getWriter().write(
                        "{\"status\":429,\"detail\":\"Muitas tentativas. Tente novamente em instantes.\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private boolean deveLimitar(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        return uri.equals("/auth/login")
                || uri.equals("/auth/register")
                || uri.equals("/auth/forgot-password")
                || uri.equals("/auth/reset-password")
                || (uri.startsWith("/public/events/") && uri.endsWith("/guest-session"));
    }

    private Bucket novoBucket() {
        Bandwidth limite = Bandwidth.builder()
                .capacity(capacidade)
                .refillGreedy(capacidade, janela)
                .build();
        return Bucket.builder().addLimit(limite).build();
    }
}
