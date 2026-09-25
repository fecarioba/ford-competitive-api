package com.fordchallenge.ford_competitive_api.vehicles.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehicleSearchRequest(

        @NotBlank(message = "Marca é obrigatória")
        String marca,

        @NotBlank(message = "Modelo é obrigatório")
        String modelo,

        @NotNull(message = "Ano é obrigatório")
        Integer ano,

        @NotBlank(message = "Versão é obrigatória")
        String versao
) {
}