package com.fordchallenge.ford_competitive_api.vehicles.dto;

public record VehicleResponse(

        Long id,

        String marca,

        String modelo,

        Integer ano,

        String versao,

        String potencia,

        String torque,

        String combustivel,

        String cambio,

        String consumo
) {
}