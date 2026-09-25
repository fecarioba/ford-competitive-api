package com.fordchallenge.ford_competitive_api.specifications.dto;

public record SpecificationResponse(

        Long vehicleId,
        String potencia,
        String torque,
        String combustivel,
        String cambio,
        String consumo,
        String fonteUrl

) {
}