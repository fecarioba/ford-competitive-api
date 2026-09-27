package com.fordchallenge.ford_competitive_api.vehicles.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record VehicleSearchRequest(

        @NotBlank(message = "Marca é obrigatória")
        @Size(max = 80)
        String marca,

        @NotBlank(message = "Modelo é obrigatório")
        @Size(max = 80)
        String modelo,

        @NotNull(message = "Ano é obrigatório")
        @Min(1886) @Max(2100)
        Integer ano,

        @NotBlank(message = "Versão é obrigatória")
        @Size(max = 80)
        String versao
) {
}
