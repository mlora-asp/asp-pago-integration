package com.asp.integration.application.port.inbound;

/**
 * Puerto para desencriptar o encriptar payloads transportados por el canal HTTP.
 *
 * El caso principal del Bridge es recibir peticiones cifradas, desencriptarlas
 * en el borde y continuar el flujo canónico ya existente.
 */
public interface PayloadCryptoPort {

    String decryptRequest(String encryptedPayload);

    String encryptResponse(String plainPayload);
}
