package com.fordchallenge.ford_competitive_api.security;

import com.fordchallenge.ford_competitive_api.common.exception.InvalidCredentialsException;
import com.fordchallenge.ford_competitive_api.users.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Centraliza a leitura do usuário autenticado a partir do SecurityContext,
 * evitando espalhar lógica de casting/validação pelos services.
 *
 * O JwtAuthenticationFilter coloca a entidade {@link User} como "principal"
 * da Authentication, então aqui apenas extraímos e validamos esse objeto.
 */
@Component
public class AuthenticatedUserProvider {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new InvalidCredentialsException("Nenhum usuário autenticado no contexto atual");
        }

        return user;
    }
}
