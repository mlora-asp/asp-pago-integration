package com.asp.integration.domain.exception;

/**
 * @autor: HJMB
 */
public class ProviderNotFoundException extends ResourceNotFoundException {
    public ProviderNotFoundException(String message) {
        super("ERROR_PROVEEDOR_NO_ENCONTRADO", message);
    }
}
