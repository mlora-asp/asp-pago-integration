package com.asp.integration.infrastructure.config;

import com.asp.integration.shared.constants.ApiPaths;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración del contrato OpenAPI / Swagger del Bridge.
 *
 * Disponible en:
 *   Swagger UI:  http://localhost:8080/swagger-ui.html
 *   OpenAPI JSON: http://localhost:8080/v3/api-docs
 *
 *
 * @autor: HJMB
 */
@Configuration
public class OpenApiConfig {

    @Value("${openapi.server.local-url:http://localhost:8080}")
    private String localServerUrl;

    @Value("${openapi.server.docker-url:http://bridge-integration:8080}")
    private String dockerServerUrl;

    @Value("${openapi.server.kong-url:http://localhost:8000/gateway}")
    private String kongServerUrl;

    @Bean
    public OpenAPI bridgeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ASP Pago Integration Bridge — API")
                        .version("1.0.0")
                        .description(description())
                        .contact(new Contact()
                                .name("Equipo ASP Pagos")
                                .email("integraciones@asp-pagos.com"))
                        .license(new License()
                                .name("Uso interno ASP — Confidencial"))
                )
                .servers(List.of(
                        new Server()
                                .url(localServerUrl)
                                .description("Desarrollo local directo al bridge"),
                        new Server()
                                .url(dockerServerUrl)
                                .description("Entorno Docker / red interna"),
                        new Server()
                                .url(kongServerUrl)
                                .description("Entrada vía Kong")
                ));
    }

    private String description() {
        return """
                                **Bridge de Integración** entre los canales de ASP Pagos y los proveedores externos.

                                Esta documentación describe la superficie REST oficial del microservicio
                                `asp-pago-integration`, alineada con los contratos OpenAPI ubicados en
                                `Nuevo ASP Pago/`. Los controllers exponen DTOs de contrato y traducen
                                internamente hacia el modelo canónico del Bridge.

                                ## Alcance de esta documentación

                                La API pública expone únicamente los endpoints definidos en los
                                contratos oficiales de ASP Pago.

                                ## Flujo de integración

                                ```
                                Frontend / Canal interno  →  Kong Gateway  →  Bridge (contrato ASP Pago)  →  Modelo canónico  →  Proveedor externo
                                                          (validación de      (DTOs REST, trazabilidad)      (routing)          (Caudex, etc.)
                                                           ingreso)
                                ```

                                ## Autenticación

                                El Bridge **no** está pensado para consumo directo desde navegador.
                                El cliente interno se autentica frente a Kong y el Bridge valida:

                                - un secreto compartido gateway → bridge
                                - el `clientId` autenticado propagado por Kong
                                - la identidad del usuario final cuando la operación lo requiere
                                - el sistema origen y el `correlationId` para trazabilidad

                                La autenticación hacia Caudex (OAuth2 client_credentials) se gestiona
                                internamente por el Bridge; los consumidores del contrato canónico no
                                necesitan conocer ese flujo.

                                ## Cobertura actual de fase 1

                                Actualmente el bridge expone la primera fase de los contratos
                                ASP Pago. Esta fase habilita el flujo inicial de autenticación,
                                onboarding y alta de beneficiario con respuestas dummy.

                                Dominios activos en esta fase:

                                - autenticación
                                - recuperación/restablecimiento de password
                                - onboarding
                                - alta de beneficiario

                                ## Modelo canónico

                                Los consumidores envían los DTOs específicos definidos por ASP Pago.
                                El adapter inbound desencripta el request, valida el contrato y
                                conserva el desacoplamiento hacia la capa de aplicación. Por ahora,
                                las respuestas son dummy para habilitar el trabajo del frontend.

                                ## Endpoints expuestos

                                - `POST %s`
                                - `POST %s`
                                - `POST %s`
                                - `POST %s`
                                - `POST %s`
                                - `POST %s`

                                ## Uso recomendado

                                - En **local**, Swagger UI puede abrirse directo en `http://localhost:8080/swagger-ui.html`
                                - En **integración/productivo**, las operaciones deben entrar por Kong
                                - La trazabilidad usa `X-Correlation-Id`; si no se recibe, el Bridge genera un UUID

                                ## Resiliencia

                                Cada llamada a proveedor tiene:
                                - **Circuit Breaker** (abre después del 50% de fallos en ventana de 10 llamadas)
                                - **Retry** (3 reintentos con backoff exponencial)
                                - **Bulkhead** (máximo 20 llamadas concurrentes por proveedor)
                                - **Time Limiter** (timeout de 10s)
                                """
                .formatted(
                        ApiPaths.AUTH_LOGIN,
                        ApiPaths.AUTH_LOGOUT,
                        ApiPaths.AUTH_PASSWORD_OTP,
                        ApiPaths.AUTH_PASSWORD_RESET,
                        ApiPaths.ONBOARDING,
                        ApiPaths.BENEFICIARIO_ALTA);
    }
}
