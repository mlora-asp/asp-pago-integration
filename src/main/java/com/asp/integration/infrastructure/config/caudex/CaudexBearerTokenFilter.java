package com.asp.integration.infrastructure.config.caudex;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import com.asp.integration.domain.exception.CaudexAuthException;

/**
 * ExchangeFilterFunction que centraliza la inyección del Bearer Token de Caudex.
 *
 * Cada vez que el caudexWebClient realiza una petición, este filtro:
 *   1. Llama a CaudexTokenService.fetchToken() — usa cache local si el token sigue vigente.
 *   2. Inyecta el token en el header Authorization: Bearer {token}.
 *   3. Intercepta HTTP 401/403 de Caudex y los convierte en CaudexAuthException.
 *   4. Si Caudex responde 401, invalida el token cacheado para forzar refresh.
 *
 * Al centralizar aquí la autenticación, los 48+ operationHandlers del CaudexClient
 * no necesitan conocer nada del flujo OAuth2 — simplemente ejecutan su lógica de negocio.
 *
 * Flujo OAuth2:
 *   CaudexTokenService → POST /oauth2/token (client_credentials) → access_token
 *
 * Este componente sólo se registra en el bean "caudexWebClient" (ver WebClientConfig).
 * El bean "caudexTokenWebClient" NO usa este filtro para evitar dependencia circular.
 *
 * @autor: HJMB
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaudexBearerTokenFilter implements ExchangeFilterFunction {

    private final CaudexTokenService tokenService;

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return tokenService.fetchToken()
                .flatMap(token -> {
                    log.debug("[CAUDEX-AUTH] Inyectando token {} en {} {}",
                            maskToken(token), request.method(), request.url());

                    ClientRequest authorizedRequest = ClientRequest.from(request)
                            .header("Authorization", "Bearer " + token)
                            .build();

                    return next.exchange(authorizedRequest);
                })
                .flatMap(this::handleAuthErrors);
    }

    /**
     * Convierte respuestas 401/403 de Caudex en excepciones tipadas.
     * El GlobalExceptionHandler las transforma en respuestas canónicas.
     */
    private Mono<ClientResponse> handleAuthErrors(ClientResponse response) {
        HttpStatus status = (HttpStatus) response.statusCode();

        if (status == HttpStatus.UNAUTHORIZED) {
            log.warn("[CAUDEX-AUTH] Token rechazado por Caudex (HTTP 401)");
            tokenService.invalidateCachedToken("Caudex respondió HTTP 401");
            return response.releaseBody()
                    .then(Mono.error(new CaudexAuthException(
                            "Token Caudex rechazado: credenciales inválidas o token expirado", 401)));
        }

        if (status == HttpStatus.FORBIDDEN) {
            log.warn("[CAUDEX-AUTH] Sin permisos en Caudex (HTTP 403)");
            return response.releaseBody()
                    .then(Mono.error(new CaudexAuthException(
                            "Sin permisos para ejecutar esta operación en Caudex", 403)));
        }

        return Mono.just(response);
    }

    /**
     * Enmascara el token para logs seguros.
     * Muestra sólo los primeros 8 caracteres para identificar el token sin exponerlo.
     * Ejemplo: "eyJhbGci..."
     */
    static String maskToken(String token) {
        if (!StringUtils.hasText(token)) return "[vacío]";
        if (token.length() <= 8) return "***";
        return token.substring(0, 8) + "...";
    }
}
