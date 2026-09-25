package com.fordchallenge.ford_competitive_api.security;

import com.fordchallenge.ford_competitive_api.users.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Responsável pela geração e validação de tokens JWT.
 *
 * A chave de assinatura e o tempo de expiração são externalizados em
 * application.properties (nunca hardcoded no código-fonte), permitindo
 * configuração por ambiente (dev, homolog, produção) sem recompilar a aplicação.
 */
@Service
public class JwtService {

    private final Key signingKey;
    private final long expirationTime;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationTime
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expirationTime;
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida assinatura e expiração do token.
     * Lança JwtException (ou subclasses, ex: ExpiredJwtException) caso o token
     * seja inválido — a exceção é tratada de forma centralizada no
     * GlobalExceptionHandler, resultando em HTTP 401.
     */
    public Claims validateAndExtractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException expiredJwtException) {
            throw expiredJwtException;
        } catch (JwtException | IllegalArgumentException exception) {
            throw new JwtException("Token JWT inválido: " + exception.getMessage());
        }
    }

    public String extractEmail(String token) {
        return validateAndExtractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return validateAndExtractClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            validateAndExtractClaims(token);
            return true;
        } catch (JwtException exception) {
            return false;
        }
    }
}
