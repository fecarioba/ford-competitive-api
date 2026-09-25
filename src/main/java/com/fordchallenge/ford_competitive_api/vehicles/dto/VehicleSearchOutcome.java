package com.fordchallenge.ford_competitive_api.vehicles.dto;

/**
 * Envelope interno (não exposto diretamente pela API) usado para o controller
 * saber se o veículo pesquisado já existia (200 OK) ou foi criado agora (201 Created),
 * permitindo status codes coerentes com a Maturidade REST Nível 2.
 */
public record VehicleSearchOutcome(
        VehicleResponse vehicle,
        boolean created
) {
}
