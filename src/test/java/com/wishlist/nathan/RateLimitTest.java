package com.wishlist.nathan;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Garante que o login é limitado por IP (Bucket4j) — após estourar a capacidade,
 * responde 429.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RateLimitTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    MockMvc mvc;

    @Test
    void loginExcedendoLimiteRetorna429() throws Exception {
        String corpo = "{\"email\":\"naoexiste@teste.com\",\"senha\":\"qualquer1\"}";
        // Capacidade configurada = 10/min: as 10 primeiras passam (401), a 11ª é bloqueada.
        for (int i = 0; i < 10; i++) {
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isTooManyRequests());
    }
}
