package com.asp.integration.infrastructure.config.webclient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.asp.integration.infrastructure.config.caudex.CaudexBearerTokenFilter;
import com.asp.integration.infrastructure.config.properties.CaudexProperties;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * Configuración centralizada de los WebClient de Caudex.
 *
 * Caudex requiere dos beans para evitar dependencia circular:
 *   "caudexTokenWebClient"  — SIN filtro Bearer, usado por CaudexTokenService
 *   "caudexWebClient"       — CON CaudexBearerTokenFilter, usado por CaudexClient
 *
 * Circular dependency que se evita:
 *   caudexWebClient → CaudexBearerTokenFilter → CaudexTokenService → caudexWebClient ✗
 *   caudexWebClient → CaudexBearerTokenFilter → CaudexTokenService → caudexTokenWebClient ✓
 *
 * @autor: HJMB
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final CaudexProperties caudexProperties;

    // ── Caudex: token WebClient (SIN filtro Bearer — para evitar dependencia circular) ──

    /**
     * WebClient exclusivo para el endpoint OAuth2 de Caudex (/oauth2/token).
     * NO tiene el CaudexBearerTokenFilter — las peticiones de token son públicas.
     *
     * Usado ÚNICAMENTE por CaudexTokenService.
     */
    @Bean("caudexTokenWebClient")
    public WebClient caudexTokenWebClient() {
        return buildWebClient(caudexProperties.getBaseUrl(),
                caudexProperties.getConnectTimeoutMs(),
                caudexProperties.getReadTimeoutMs(),
                "CAUDEX-TOKEN");
    }

    // ── Caudex: API WebClient (CON filtro Bearer — para todos los endpoints de negocio) ──

    /**
     * WebClient para los endpoints de negocio Caudex activos en contratos ASP Pago.
     * Tiene el CaudexBearerTokenFilter que inyecta automáticamente el Bearer token.
     */
    @Bean("caudexWebClient")
    public WebClient caudexWebClient(CaudexBearerTokenFilter bearerTokenFilter) {
        return buildWebClient(caudexProperties.getBaseUrl(),
                caudexProperties.getConnectTimeoutMs(),
                caudexProperties.getReadTimeoutMs(),
                "CAUDEX")
                .mutate()
                .filter(bearerTokenFilter)
                .build();
    }

    // ── Builder compartido ────────────────────────────────────────────────────

    private WebClient buildWebClient(String baseUrl, int connectTimeoutMs,
                                     int readTimeoutMs, String providerName) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofMillis(readTimeoutMs))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(5_000, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest(providerName))
                .filter(logResponse(providerName))
                .build();
    }

    private ExchangeFilterFunction logRequest(String provider) {
        return ExchangeFilterFunction.ofRequestProcessor(req -> {
            log.debug("[{}] --> {} {}", provider, req.method(), req.url());
            return Mono.just(req);
        });
    }

    private ExchangeFilterFunction logResponse(String provider) {
        return ExchangeFilterFunction.ofResponseProcessor(resp -> {
            log.debug("[{}] <-- HTTP {}", provider, resp.statusCode());
            return Mono.just(resp);
        });
    }
}
