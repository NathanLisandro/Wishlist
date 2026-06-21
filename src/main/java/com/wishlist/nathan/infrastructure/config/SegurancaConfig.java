package com.wishlist.nathan.infrastructure.config;

import com.wishlist.nathan.infrastructure.security.FiltroAutenticacaoJwt;
import com.wishlist.nathan.infrastructure.security.FiltroRateLimit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Segurança stateless (JWT). Rotas públicas: registro/login do admin, criação de
 * sessão do convidado e webhook. Demais rotas exigem o papel adequado.
 */
@Configuration
public class SegurancaConfig {

    private final AppProperties props;

    public SegurancaConfig(AppProperties props) {
        this.props = props;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, FiltroAutenticacaoJwt filtroJwt) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API stateless com Bearer token
                .cors(cors -> cors.configurationSource(corsSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Públicas
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/verify-email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/public/events/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/public/events/*/guest-session").permitAll()
                        .requestMatchers("/webhooks/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        // Fotos servidas estaticamente (foto do casal na landing)
                        .requestMatchers(HttpMethod.GET, mediaPattern()).permitAll()
                        // Convidado
                        .requestMatchers("/public/**").hasRole("GUEST")
                        // Admin (todo o resto)
                        .anyRequest().hasRole("ADMIN"))
                .addFilterBefore(new FiltroRateLimit(10, java.time.Duration.ofMinutes(1)), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        // 'self' para conteúdo; img-src libera as fotos servidas e data: (placeholders).
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; img-src 'self' data: blob:")));
        return http.build();
    }

    /** Padrão do prefixo público das fotos (ex.: /media/**). */
    private String mediaPattern() {
        return props.storage().publicBase().replaceAll("/+$", "") + "/**";
    }

    private CorsConfigurationSource corsSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> padroes = new java.util.ArrayList<>(Arrays.stream(props.cors().allowedOrigins().split(","))
                .map(String::trim).filter(s -> !s.isBlank()).toList());
        // Túneis de teste no celular (ngrok) — o subdomínio muda a cada sessão, então
        // liberamos por padrão de origem (allowedOriginPatterns aceita curinga com credenciais).
        padroes.addAll(List.of(
                "https://*.ngrok-free.app", "https://*.ngrok.app", "https://*.ngrok.io",
                "https://*.ngrok-free.dev"));
        config.setAllowedOriginPatterns(padroes);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
