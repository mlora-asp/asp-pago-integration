package com.asp.integration.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del esquema de cifrado del canal ASP Pago.
 */
@Data
@ConfigurationProperties(prefix = "security.crypto")
public class CryptoProperties {

    /**
     * Permite activar el soporte de payload cifrado en el borde HTTP.
     */
    private boolean enabled = false;

    /**
     * Llave base compartida con el cliente emisor. Se deriva internamente a key/iv.
     */
    private String secretKey = "";

    /**
     * Charset esperado por el payload antes del URL decode.
     */
    private String encoding = "UTF-8";
}
