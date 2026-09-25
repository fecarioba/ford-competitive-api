package com.fordchallenge.ford_competitive_api.searches.dto;

import java.time.LocalDateTime;

public record SearchHistoryResponse(
        Long id,
        String termoBusca,
        Long userId,
        Long vehicleId,
        String marca,
        String modelo,
        Integer ano,
        String versao,
        LocalDateTime createdAt
) {
}