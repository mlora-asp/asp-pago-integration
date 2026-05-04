package com.asp.integration.domain.exception;

import com.asp.integration.shared.constants.ResponseCodes;

/**
 * @autor: HJMB
 */
public class ProviderNotFoundException extends ResourceNotFoundException {
    public ProviderNotFoundException(String message) {
        super(ResponseCodes.ERROR_PROVEEDOR_NO_ENCONTRADO, message);
    }
}
