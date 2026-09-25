package com.fordchallenge.ford_competitive_api.users.dto;

import com.fordchallenge.ford_competitive_api.users.entity.User;
import com.fordchallenge.ford_competitive_api.users.entity.UserRole;

import java.time.LocalDateTime;

/**
 * DTO de saída para dados de usuário.
 *
 * Correção de segurança: os endpoints de /auth passavam a retornar a entidade
 * User diretamente, o que expunha o campo senhaHash (hash BCrypt da senha) nas
 * respostas JSON. Este DTO garante que apenas dados não sensíveis sejam expostos.
 */
public record UserResponse(
        Long id,
        String nome,
        String email,
        UserRole role,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
