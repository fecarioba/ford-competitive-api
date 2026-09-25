package com.fordchallenge.ford_competitive_api.common.exception;

/**
 * Lançada quando um recurso solicitado não é encontrado.
 * Mapeada para HTTP 404 (Not Found) pelo GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
