package com.asp.integration.domain.exception;

import com.asp.integration.shared.constants.ResponseCodes;

/**
 * Señala fallos al normalizar o desencriptar payloads cifrados recibidos por el Bridge.
 */
public class PayloadDecryptionException extends BadRequestException {

    public PayloadDecryptionException(String message) {
        super(ResponseCodes.ERROR_PAYLOAD_CIFRADO, message);
    }

    public PayloadDecryptionException(String message, Throwable cause) {
        super(ResponseCodes.ERROR_PAYLOAD_CIFRADO, message, cause);
    }
}
