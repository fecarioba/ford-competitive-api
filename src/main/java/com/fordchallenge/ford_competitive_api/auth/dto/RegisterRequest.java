package com.fordchallenge.ford_competitive_api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120)
        String nome,

        @Email(message = "Email inválido")
        @NotBlank(message = "O email é obrigatório")
        @Size(max = 254)
        String email,

        @Size(min = 12, max = 128, message = "A senha deve ter entre 12 e 128 caracteres")
        @NotBlank(message = "A senha é obrigatória")
        String senha
) {
}
