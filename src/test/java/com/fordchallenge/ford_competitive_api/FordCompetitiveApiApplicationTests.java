package com.fordchallenge.ford_competitive_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Usa o perfil "test" (H2 em memória) para que o contexto suba sem depender
// de um PostgreSQL rodando localmente durante o build/CI.
@SpringBootTest
@ActiveProfiles("test")
class FordCompetitiveApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
