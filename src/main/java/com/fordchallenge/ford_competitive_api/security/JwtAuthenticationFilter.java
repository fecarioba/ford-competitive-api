package com.fordchallenge.ford_competitive_api.security;

import com.fordchallenge.ford_competitive_api.users.entity.User;
import com.fordchallenge.ford_competitive_api.users.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que intercepta todas as requisições e, quando um Bearer Token válido
 * é encontrado, autentica o usuário no contexto de segurança do Spring.
 *
 * Se o token estiver ausente, expirado, malformado ou com assinatura inválida,
 * a requisição simplesmente segue como "anônima": não lançamos exceção aqui
 * (este filtro roda antes do ExceptionTranslationFilter). Endpoints protegidos
 * serão então rejeitados mais adiante pela regra de autorização, resultando em
 * HTTP 401 via {@link JwtAuthenticationEntryPoint}.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtService.extractEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository.findByEmail(email).orElse(null);

                if (user != null) {
                    var authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                    );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                    authentication.setDetails(request);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (JwtException expiredOrInvalidToken) {
            // Token inválido/expirado: contexto de segurança permanece vazio.
            // Endpoints protegidos retornarão 401 automaticamente.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
