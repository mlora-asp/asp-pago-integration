package com.asp.integration.infrastructure.config.caudex;

import com.asp.integration.infrastructure.config.properties.CaudexProperties;
import com.asp.integration.domain.exception.CaudexAuthException;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * @autor: HJMB
 */
@Slf4j
@Service
public class CaudexTokenService {

    private final WebClient tokenWebClient;
    private final CaudexProperties caudexProperties;
    private final Duration refreshSkew;
    private final Counter tokenCacheHitCounter;
    private final Counter tokenRefreshSuccessCounter;
    private final Counter tokenRefreshFailureCounter;
    private final Counter tokenInvalidatedCounter;
    private final Object refreshMonitor = new Object();

    private volatile CachedToken cachedToken;
    private volatile Mono<String> inFlightRefresh;

    public CaudexTokenService(
            @Qualifier("caudexTokenWebClient") WebClient tokenWebClient,
            CaudexProperties caudexProperties,
            MeterRegistry meterRegistry) {
        this.tokenWebClient = tokenWebClient;
        this.caudexProperties = caudexProperties;
        this.refreshSkew = Duration.ofSeconds(caudexProperties.getToken().getRefreshSkewSeconds());
        this.tokenCacheHitCounter = Counter.builder("caudex.token.cache.hit")
                .description("Caudex token served from local in-memory cache")
                .register(meterRegistry);
        this.tokenRefreshSuccessCounter = Counter.builder("caudex.token.refresh.success")
                .description("Caudex token refresh completed successfully")
                .register(meterRegistry);
        this.tokenRefreshFailureCounter = Counter.builder("caudex.token.refresh.failure")
                .description("Caudex token refresh failed")
                .register(meterRegistry);
        this.tokenInvalidatedCounter = Counter.builder("caudex.token.invalidated")
                .description("Caudex token cache invalidated")
                .register(meterRegistry);
    }

    public Mono<String> fetchToken() {
        CachedToken token = cachedToken;
        Instant now = Instant.now();
        if (token != null && token.isValid(now, refreshSkew)) {
            tokenCacheHitCounter.increment();
            log.debug("[CAUDEX-TOKEN] token cache hit; expiresAt={}", token.expiresAt());
            return Mono.just(token.accessToken());
        }

        synchronized (refreshMonitor) {
            token = cachedToken;
            now = Instant.now();
            if (token != null && token.isValid(now, refreshSkew)) {
                tokenCacheHitCounter.increment();
                log.debug("[CAUDEX-TOKEN] token cache hit after lock; expiresAt={}", token.expiresAt());
                return Mono.just(token.accessToken());
            }

            if (inFlightRefresh != null) {
                log.debug("[CAUDEX-TOKEN] reutilizando refresh concurrente en curso");
                return inFlightRefresh;
            }

            inFlightRefresh = fetchFreshToken()
                    .doOnNext(this::cacheToken)
                    .map(CachedToken::accessToken)
                    .doOnError(ex -> {
                        tokenRefreshFailureCounter.increment();
                        log.error("[CAUDEX-TOKEN] token refresh failure: {}", ex.getMessage());
                    })
                    .doFinally(signalType -> {
                        synchronized (refreshMonitor) {
                            inFlightRefresh = null;
                        }
                    })
                    .cache();

            return inFlightRefresh;
        }
    }

    public void invalidateCachedToken(String reason) {
        synchronized (refreshMonitor) {
            cachedToken = null;
            inFlightRefresh = null;
        }
        tokenInvalidatedCounter.increment();
        log.warn("[CAUDEX-TOKEN] token invalidated: {}", reason);
    }

    private Mono<CachedToken> fetchFreshToken() {
        log.debug("[CAUDEX-TOKEN] Solicitando token OAuth2 → {}/oauth2/token",
                caudexProperties.getBaseUrl());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", caudexProperties.getClientId());
        formData.add("client_secret", caudexProperties.getClientSecret());
        formData.add("scope", caudexProperties.getScope());

        return tokenWebClient
                .post()
                .uri("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(CaudexTokenResponse.class)
                .flatMap(this::extractAndValidateToken)
                .onErrorResume(WebClientResponseException.class, this::handleTokenHttpError)
                .doOnError(ex ->
                        log.error("[CAUDEX-TOKEN] Error obteniendo token: {}", ex.getMessage()));
    }

    private Mono<CachedToken> extractAndValidateToken(CaudexTokenResponse response) {
        String token = response.getAccessToken();
        if (!StringUtils.hasText(token)) {
            log.error("[CAUDEX-TOKEN] Caudex devolvió token vacío o nulo");
            return Mono.error(new CaudexAuthException(
                    "Caudex devolvió un token vacío — verificar configuración OAuth2"));
        }
        log.debug("[CAUDEX-TOKEN] Token obtenido exitosamente (expires_in={}s, type={})",
                response.getExpiresIn(), response.getTokenType());
        long expiresIn = resolveExpiresIn(response);
        return Mono.just(new CachedToken(token, Instant.now().plusSeconds(expiresIn)));
    }

    private long resolveExpiresIn(CaudexTokenResponse response) {
        Long expiresIn = response.getExpiresIn();
        if (expiresIn == null || expiresIn <= 0) {
            long fallback = caudexProperties.getToken().getDefaultExpiresInSeconds();
            log.warn("[CAUDEX-TOKEN] Caudex no envió expires_in válido; usando default={}s", fallback);
            return fallback;
        }
        return expiresIn;
    }

    private void cacheToken(CachedToken token) {
        cachedToken = token;
        tokenRefreshSuccessCounter.increment();
        log.info("[CAUDEX-TOKEN] token refresh success; expiresAt={}", token.expiresAt());
    }

    private Mono<CachedToken> handleTokenHttpError(WebClientResponseException ex) {
        int status = ex.getStatusCode().value();
        log.error("[CAUDEX-TOKEN] HTTP {} al obtener token OAuth2", status);

        return switch (status) {
            case 400 -> Mono.error(new CaudexAuthException(
                    "Parámetros OAuth2 inválidos (grant_type, scope)", status));
            case 401 -> Mono.error(new CaudexAuthException(
                    "Credenciales Caudex inválidas (client_id/client_secret)", status));
            case 403 -> Mono.error(new CaudexAuthException(
                    "Sin permisos para obtener token Caudex", status));
            default -> Mono.error(new CaudexAuthException(
                    "Error inesperado al obtener token Caudex HTTP " + status, status));
        };
    }

    @Data
    static class CaudexTokenResponse {

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private Long expiresIn;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("scope")
        private String scope;
    }

    private record CachedToken(String accessToken, Instant expiresAt) {
        boolean isValid(Instant now, Duration refreshSkew) {
            return StringUtils.hasText(accessToken) && now.plus(refreshSkew).isBefore(expiresAt);
        }
    }
}
