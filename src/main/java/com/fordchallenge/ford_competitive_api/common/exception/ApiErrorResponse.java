package com.fordchallenge.ford_competitive_api.common.exception;

import java.time.LocalDateTime;

/**
 * Formato padronizado de resposta de erro da API.
 *
 * @param status    código HTTP numérico (ex: 404)
 * @param error     nome curto do erro (ex: "NOT_FOUND")
 * @param message   mensagem legível explicando o problema
 * @param path      endpoint onde o erro ocorreu
 * @param timestamp momento em que o erro ocorreu
 */
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp
) {
}
