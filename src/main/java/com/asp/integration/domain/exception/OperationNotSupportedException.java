package com.asp.integration.domain.exception;

import com.asp.integration.shared.constants.ResponseCodes;

/**
 * @autor: HJMB
 */
public class OperationNotSupportedException extends BadRequestException {
    public OperationNotSupportedException(String message) {
        super(ResponseCodes.ERROR_OPERACION_NO_SOPORTADA, message);
    }
}
