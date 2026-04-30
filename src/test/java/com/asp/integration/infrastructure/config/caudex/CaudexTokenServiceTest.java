package com.asp.integration.infrastructure.config.caudex;

import com.asp.integration.domain.exception.CaudexAuthException;
import com.asp.integration.infrastructure.config.properties.CaudexProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class CaudexTokenServiceTest {

    private MockWebServer mockCaudexServer;
    private SimpleMeterRegistry meterRegistry;
    private CaudexTokenService tokenService;

    @BeforeEach
    void setUp() throws IOException {
        mockCaudexServer = new MockWebServer();
        mockCaudexServer.start();
        meterRegistry = new SimpleMeterRegistry();

        CaudexProperties caudexProperties = new CaudexProperties();
        caudexProperties.setBaseUrl(mockCaudexServer.url("/").toString());
        caudexProperties.setClientId("client-id");
        caudexProperties.setClientSecret("client-secret");
        caudexProperties.getToken().setRefreshSkewSeconds(120);
        caudexProperties.getToken().setDefaultExpiresInSeconds(1799);

        WebClient tokenWebClient = WebClient.builder()
                .baseUrl(mockCaudexServer.url("/").toString())
                .build();

        tokenService = new CaudexTokenService(tokenWebClient, caudexProperties, meterRegistry);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockCaudexServer.shutdown();
    }

    @Test
    void fetchToken_reusesCachedTokenBeforeRefreshSkew() {
        enqueueToken("token-uno", 1799);

        StepVerifier.create(tokenService.fetchToken())
                .expectNext("token-uno")
                .verifyComplete();

        StepVerifier.create(tokenService.fetchToken())
                .expectNext("token-uno")
                .verifyComplete();

        assertThat(mockCaudexServer.getRequestCount()).isEqualTo(1);
        assertThat(counter("caudex.token.refresh.success")).isEqualTo(1.0);
        assertThat(counter("caudex.token.cache.hit")).isEqualTo(1.0);
    }

    @Test
    void fetchToken_renewsWhenTokenIsInsideRefreshSkew() {
        enqueueToken("token-corto", 60);
        enqueueToken("token-renovado", 1799);

        StepVerifier.create(tokenService.fetchToken())
                .expectNext("token-corto")
                .verifyComplete();

        StepVerifier.create(tokenService.fetchToken())
                .expectNext("token-renovado")
                .verifyComplete();

        assertThat(mockCaudexServer.getRequestCount()).isEqualTo(2);
        assertThat(counter("caudex.token.refresh.success")).isEqualTo(2.0);
        assertThat(counter("caudex.token.cache.hit")).isZero();
    }

    @Test
    void fetchToken_sharesSingleConcurrentRefresh() {
        enqueueToken("token-compartido", 1799);

        Mono<String> first = tokenService.fetchToken();
        Mono<String> second = tokenService.fetchToken();

        StepVerifier.create(Mono.zip(first, second))
                .assertNext(tokens -> {
                    assertThat(tokens.getT1()).isEqualTo("token-compartido");
                    assertThat(tokens.getT2()).isEqualTo("token-compartido");
                })
                .verifyComplete();

        assertThat(mockCaudexServer.getRequestCount()).isEqualTo(1);
        assertThat(counter("caudex.token.refresh.success")).isEqualTo(1.0);
    }

    @Test
    void invalidateCachedToken_forcesNextRefresh() {
        enqueueToken("token-original", 1799);
        enqueueToken("token-nuevo", 1799);

        StepVerifier.create(tokenService.fetchToken())
                .expectNext("token-original")
                .verifyComplete();

        tokenService.invalidateCachedToken("test");

        StepVerifier.create(tokenService.fetchToken())
                .expectNext("token-nuevo")
                .verifyComplete();

        assertThat(mockCaudexServer.getRequestCount()).isEqualTo(2);
        assertThat(counter("caudex.token.invalidated")).isEqualTo(1.0);
    }

    @Test
    void fetchToken_refreshFailureIncrementsMetric() {
        mockCaudexServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{\"error\":\"invalid_client\"}")
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(tokenService.fetchToken())
                .expectError(CaudexAuthException.class)
                .verify();

        assertThat(counter("caudex.token.refresh.failure")).isEqualTo(1.0);
    }

    private void enqueueToken(String token, long expiresIn) {
        mockCaudexServer.enqueue(new MockResponse()
                .setBody("""
                        {
                          "access_token": "%s",
                          "expires_in": %d,
                          "token_type": "Bearer",
                          "scope": "message.read"
                        }
                        """.formatted(token, expiresIn))
                .addHeader("Content-Type", "application/json"));
    }

    private double counter(String name) {
        return meterRegistry.find(name).counter().count();
    }
}
