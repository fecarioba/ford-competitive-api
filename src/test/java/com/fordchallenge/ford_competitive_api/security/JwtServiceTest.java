package com.fordchallenge.ford_competitive_api.security;

import com.fordchallenge.ford_competitive_api.users.entity.User;
import com.fordchallenge.ford_competitive_api.users.entity.UserRole;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários da geração e validação de JWT (Sprint 3 - critério "JWT").
 * Cobre: geração/extração de claims, expiração e proteção contra assinatura adulterada.
 */
class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-key-1234567890-abcdefghij";

    private final JwtService jwtService = new JwtService(SECRET, 1000L * 60 * 60);

    private User buildUser() {
        return User.builder()
                .id(1L)
                .nome("Victor Hugo")
                .email("victor@test.com")
                .role(UserRole.USER)
                .build();
    }

    @Test
    void deveGerarTokenEExtrairEmailERole() {
        User user = buildUser();

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo("victor@test.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("USER");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void deveConsiderarTokenExpiradoComoInvalido() {
        // expiração negativa: o token já nasce expirado
        JwtService jwtServiceExpirado = new JwtService(SECRET, -1000L);

        String token = jwtServiceExpirado.generateToken(buildUser());

        assertThat(jwtServiceExpirado.isTokenValid(token)).isFalse();
        assertThatThrownBy(() -> jwtServiceExpirado.extractEmail(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void deveRejeitarTokenAssinadoComChaveDiferente() {
        String token = jwtService.generateToken(buildUser());

        JwtService outroServico = new JwtService(
                "outra-chave-secreta-completamente-diferente-000",
                1000L * 60 * 60
        );

        assertThat(outroServico.isTokenValid(token)).isFalse();
        assertThatThrownBy(() -> outroServico.extractEmail(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void deveRejeitarTokenMalformado() {
        assertThat(jwtService.isTokenValid("token-completamente-invalido")).isFalse();
    }
}
