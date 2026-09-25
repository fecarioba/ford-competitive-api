package com.fordchallenge.ford_competitive_api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @Email(message = "Email inválido")
        @NotBlank(message = "O email é obrigatório")
        String email,

        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        @NotBlank(message = "A senha é obrigatória")
        String senha
) {
}