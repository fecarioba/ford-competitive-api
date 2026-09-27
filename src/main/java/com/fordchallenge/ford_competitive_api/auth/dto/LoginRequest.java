package com.fordchallenge.ford_competitive_api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @Email(message = "Email inválido")
        @NotBlank(message = "O email é obrigatório")
        @Size(max = 254)
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(max = 128)
        String senha
) {
}
