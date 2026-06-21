package com.wishlist.nathan.infrastructure.security;

import com.wishlist.nathan.application.dto.SessaoAdmin;
import com.wishlist.nathan.application.dto.SessaoConvidado;
import com.wishlist.nathan.application.port.TokenSessaoPort;
import com.wishlist.nathan.infrastructure.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Emissão/validação de JWT (HS256) para sessões de admin e convidado.
 * A sessão do convidado é escopada a {@code (eventoId, convidadoId)}.
 */
@Component
public class ServicoTokenJwt implements TokenSessaoPort {

    private static final String CLAIM_TIPO = "tipo";
    private static final String CLAIM_EVENTO = "eventoId";
    private static final String TIPO_ADMIN = "ADMIN";
    private static final String TIPO_CONVIDADO = "CONVIDADO";
    private static final Duration VALIDADE_ADMIN = Duration.ofHours(2);
    private static final Duration VALIDADE_CONVIDADO = Duration.ofHours(6);

    private final String segredo;
    private volatile SecretKey chave;

    public ServicoTokenJwt(AppProperties props) {
        this.segredo = props.security().jwtSecret();
    }

    @Override
    public String emitirTokenAdmin(UUID contaId) {
        return construir(contaId.toString(), TIPO_ADMIN, null, VALIDADE_ADMIN);
    }

    @Override
    public String emitirTokenConvidado(UUID eventoId, UUID convidadoId) {
        return construir(convidadoId.toString(), TIPO_CONVIDADO, eventoId.toString(), VALIDADE_CONVIDADO);
    }

    @Override
    public SessaoAdmin validarAdmin(String token) {
        Claims claims = analisar(token);
        if (!TIPO_ADMIN.equals(claims.get(CLAIM_TIPO, String.class))) {
            throw new SessaoInvalidaException("Token não é de admin");
        }
        return new SessaoAdmin(UUID.fromString(claims.getSubject()));
    }

    @Override
    public SessaoConvidado validarConvidado(String token) {
        Claims claims = analisar(token);
        if (!TIPO_CONVIDADO.equals(claims.get(CLAIM_TIPO, String.class))) {
            throw new SessaoInvalidaException("Token não é de convidado");
        }
        return new SessaoConvidado(
                UUID.fromString(claims.get(CLAIM_EVENTO, String.class)),
                UUID.fromString(claims.getSubject()));
    }

    private String construir(String subject, String tipo, String eventoId, Duration validade) {
        Instant agora = Instant.now();
        var builder = Jwts.builder()
                .subject(subject)
                .claim(CLAIM_TIPO, tipo)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(validade)))
                .signWith(chave());
        if (eventoId != null) {
            builder.claim(CLAIM_EVENTO, eventoId);
        }
        return builder.compact();
    }

    private Claims analisar(String token) {
        try {
            return Jwts.parser().verifyWith(chave()).build()
                    .parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            throw new SessaoInvalidaException("Token inválido ou expirado");
        }
    }

    /** Deriva uma chave HS256 de 256 bits a partir do segredo (carga sob demanda). */
    private SecretKey chave() {
        SecretKey local = chave;
        if (local == null) {
            synchronized (this) {
                if (chave == null) {
                    if (segredo == null || segredo.isBlank()) {
                        throw new IllegalStateException("JWT_SECRET não configurado");
                    }
                    try {
                        byte[] material = MessageDigest.getInstance("SHA-256")
                                .digest(segredo.getBytes(StandardCharsets.UTF_8));
                        chave = Keys.hmacShaKeyFor(material);
                    } catch (Exception e) {
                        throw new IllegalStateException("Falha ao derivar chave JWT", e);
                    }
                }
                local = chave;
            }
        }
        return local;
    }
}
