package com.asp.integration.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.HashMap;
import java.util.Map;

/**
 * Propiedades de configuración del proveedor Caudex Core Banking.
 *
 * Cargadas desde application.yml bajo el prefijo {@code providers.caudex}.
 * Usando @ConfigurationProperties en lugar de @Value dispersos garantiza:
 *  - Validación automática al arrancar (fail-fast)
 *  - Agrupación coherente y refactorizable
 *  - Soporte para relaxed-binding (kebab-case, snake_case, camelCase)
 *
 * Variables de entorno requeridas:
 *   CAUDEX_BASE_URL
 *   CAUDEX_CLIENT_ID
 *   CAUDEX_CLIENT_SECRET
 *
 * @autor: HJMB
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "providers.caudex")
public class CaudexProperties {

    /**
     * URL base de Caudex. El endpoint OAuth2 y todos los API endpoints
     * comparten el mismo host base.
     */
    @NotBlank(message = "providers.caudex.base-url es obligatorio")
    private String baseUrl;

    /**
     * Client ID para autenticación OAuth2 client_credentials.
     * Configurar vía variable de entorno CAUDEX_CLIENT_ID.
     */
    @NotBlank(message = "providers.caudex.client-id es obligatorio")
    private String clientId;

    /**
     * Client Secret para autenticación OAuth2 client_credentials.
     * Configurar vía variable de entorno CAUDEX_CLIENT_SECRET.
     * NUNCA hardcodear en código fuente.
     */
    @NotBlank(message = "providers.caudex.client-secret es obligatorio")
    private String clientSecret;

    /**
     * Scope OAuth2 requerido por Caudex.
     * Valor por defecto: "message.read" (configurado en Caudex Authorization Server).
     */
    private String scope = "message.read";

    /** Timeout de conexión TCP en milisegundos. */
    @Positive
    private int connectTimeoutMs = 5000;

    /** Timeout de lectura de respuesta en milisegundos. */
    @Positive
    private int readTimeoutMs = 15000;

    /**
     * Configuración del cache local del access token OAuth2 de Caudex.
     */
    private Token token = new Token();

    /**
     * Endpoints de negocio de Caudex indexados por operationType.
     * Mantener estas rutas fuera del codigo permite cambiar paths por ambiente
     * sin recompilar el Bridge.
     */
    @NotEmpty(message = "providers.caudex.endpoints es obligatorio")
    private Map<String, @NotBlank String> endpoints = new HashMap<>();

    public String endpointFor(String operationType) {
        String endpoint = endpoints.get(operationType);
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException(
                    "Endpoint Caudex no configurado para operacion: " + operationType);
        }
        return endpoint;
    }

    @Data
    public static class Token {

        /**
         * Margen de renovación anticipada. Si el token vence dentro de esta ventana,
         * se solicita uno nuevo antes de llamar a Caudex.
         */
        @Positive
        private long refreshSkewSeconds = 120;

        /**
         * Duración usada cuando Caudex no envía expires_in.
         */
        @Positive
        private long defaultExpiresInSeconds = 1799;
    }
}
