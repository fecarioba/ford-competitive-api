package com.fordchallenge.ford_competitive_api.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType
) {
}