package com.fordchallenge.ford_competitive_api.common.exception;

/**
 * Lançada quando as credenciais informadas no login são inválidas.
 * Mapeada para HTTP 401 (Unauthorized) pelo GlobalExceptionHandler.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
