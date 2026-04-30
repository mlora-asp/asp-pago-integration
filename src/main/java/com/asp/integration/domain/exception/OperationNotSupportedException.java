package com.asp.integration.domain.exception;

/**
 * @autor: HJMB
 */
public class OperationNotSupportedException extends BadRequestException {
    public OperationNotSupportedException(String message) {
        super("ERROR_OPERACION_NO_SOPORTADA", message);
    }
}
