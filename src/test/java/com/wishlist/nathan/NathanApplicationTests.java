package com.wishlist.nathan;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de fumaça: sobe o contexto Spring contra um Postgres real (Testcontainers),
 * o que força o Flyway a aplicar V1 + V2. Valida que o schema e a seed existem.
 * Requer Docker disponível na máquina/CI.
 */
@SpringBootTest
@ActiveProfiles("prod") // usa logging estruturado; datasource vem do container abaixo
@Testcontainers
class NathanApplicationTests {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

	@Autowired
	JdbcTemplate jdbc;

	@Test
	void contextLoads() {
		// Se chegou aqui, o contexto subiu e o Flyway migrou sem erro.
	}

	@Test
	void migrationsAplicadasESeedPresente() {
		Integer categorias = jdbc.queryForObject(
				"SELECT count(*) FROM event_category", Integer.class);
		assertThat(categorias).isEqualTo(9);

		// Tabela de rastreamento (V2) deve existir.
		Integer acessos = jdbc.queryForObject(
				"SELECT count(*) FROM guest_access_log", Integer.class);
		assertThat(acessos).isZero();

		// Versões aplicadas pelo Flyway.
		Integer migracoes = jdbc.queryForObject(
				"SELECT count(*) FROM flyway_schema_history WHERE success = true", Integer.class);
		assertThat(migracoes).isGreaterThanOrEqualTo(2);
	}
}
