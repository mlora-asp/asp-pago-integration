package com.asp.integration.infrastructure.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuración de seguridad para el ingreso al Bridge desde Kong.
 *
 * Define el contrato mínimo que debe cumplir Kong al llamar a este servicio:
 *   - secreto compartido Kong -> Bridge
 *   - client id autenticado del emisor JWT / consumer validado por Kong
 *   - identidad del usuario final derivada del JWT o propagada por Kong
 *
 * @autor: HJMB
 */
@Data
@Validated
@ConfigurationProperties(prefix = "security.ingress")
public class IngressSecurityProperties {

    /**
     * Permite desactivar el filtro en escenarios muy controlados de prueba.
     */
    private boolean enabled = true;

    /**
     * Header interno inyectado por Kong para demostrar que la petición cruzó el gateway.
     */
    @NotBlank
    private String gatewaySecretHeader = "X-Gateway-Secret";

    /**
     * Secreto compartido entre Kong y el Bridge.
     * Debe venir de variable de entorno.
     */
    @NotBlank
    private String gatewaySharedSecret;

    /**
     * Header que identifica a la aplicación autenticada frente a Kong.
     */
    @NotBlank
    private String authenticatedClientHeader = "X-Authenticated-Client";

    /**
     * Header con el subject / id del usuario autenticado.
     */
    @NotBlank
    private String authenticatedUserHeader = "X-Authenticated-User";

    /**
     * Header con scopes o permisos efectivos del usuario final.
     */
    @NotBlank
    private String authenticatedScopesHeader = "X-Authenticated-Scopes";

    /**
     * Header de trazabilidad funcional del sistema origen.
     */
    @NotBlank
    private String systemOriginHeader = "X-Sistema-Origen";

    /**
     * Lista blanca de clientes internos permitidos a entrar al Bridge.
     */
    private List<String> allowedClientIds = new ArrayList<>(List.of("asp-pagos-auth-service"));

    /**
     * Exige identidad de usuario final en los endpoints de operación.
     */
    private boolean requireAuthenticatedUserForOperations = true;

    /**
     * Rutas que quedan fuera del control de ingreso para facilitar health checks y documentación.
     */
    private List<String> publicPathPrefixes = new ArrayList<>(List.of(
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/error"
    ));
}
