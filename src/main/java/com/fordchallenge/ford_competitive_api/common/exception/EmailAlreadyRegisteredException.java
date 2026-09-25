package com.fordchallenge.ford_competitive_api.common.exception;

/**
 * Lançada ao tentar registrar um e-mail que já existe na base.
 * Mapeada para HTTP 409 (Conflict) pelo GlobalExceptionHandler.
 */
public class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException(String message) {
        super(message);
    }
}
