package com.asp.integration.domain.exception;

/**
 * Señala fallos al normalizar o desencriptar payloads cifrados recibidos por el Bridge.
 */
public class PayloadDecryptionException extends BadRequestException {

    public PayloadDecryptionException(String message) {
        super("ERROR_PAYLOAD_CIFRADO", message);
    }

    public PayloadDecryptionException(String message, Throwable cause) {
        super("ERROR_PAYLOAD_CIFRADO", message, cause);
    }
}
