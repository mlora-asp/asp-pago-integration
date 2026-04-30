package com.asp.integration.infrastructure.config.caudex;

import com.asp.integration.domain.exception.CaudexAuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Tests para CaudexBearerTokenFilter.
 *
 * Verifica que:
 *  - El token se inyecta en el header Authorization antes de cada petición
 *  - HTTP 401 se convierte en CaudexAuthException con httpStatus=401
 *  - HTTP 403 se convierte en CaudexAuthException con httpStatus=403
 *  - Errores del tokenService se propagan correctamente
 *  - El método maskToken funciona correctamente para logs seguros
 */
@ExtendWith(MockitoExtension.class)
class CaudexBearerTokenFilterTest {

    @Mock
    private CaudexTokenService tokenService;

    @Mock
    private ExchangeFunction exchangeFunction;

    private CaudexBearerTokenFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CaudexBearerTokenFilter(tokenService);
    }

    // ── Inyección correcta del token ──────────────────────────────────────────

    @Test
    void filter_injectsBearerTokenInRequest() {
        String testToken = "eyJhbGciOiJSUzI1NiJ9.test";
        ClientRequest originalRequest = ClientRequest.create(
                org.springframework.http.HttpMethod.POST,
                URI.create("https://api.caudex.mx:12443/api/depositos-vista/consultaSaldos"))
                .build();

        ClientResponse mockResponse = mock(ClientResponse.class);
        when(mockResponse.statusCode()).thenReturn(HttpStatus.OK);
        when(tokenService.fetchToken()).thenReturn(Mono.just(testToken));
        when(exchangeFunction.exchange(argThat(req ->
                ("Bearer " + testToken).equals(req.headers().getFirst("Authorization"))
        ))).thenReturn(Mono.just(mockResponse));

        StepVerifier.create(filter.filter(originalRequest, exchangeFunction))
                .assertNext(response -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();

        verify(tokenService).fetchToken();
        verify(exchangeFunction).exchange(argThat(req ->
                ("Bearer " + testToken).equals(req.headers().getFirst("Authorization"))));
    }

    @Test
    void filter_injectsTokenOnEveryCall() {
        ClientRequest originalRequest = ClientRequest.create(
                org.springframework.http.HttpMethod.POST,
                URI.create("https://api.caudex.mx:12443/api/clientes/datosBasicos/consulta"))
                .build();

        ClientResponse mockResponse = mock(ClientResponse.class);
        when(mockResponse.statusCode()).thenReturn(HttpStatus.OK);
        when(tokenService.fetchToken()).thenReturn(Mono.just("token-fresh-01"))
                .thenReturn(Mono.just("token-fresh-02"));
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(mockResponse));

        // Primera llamada
        StepVerifier.create(filter.filter(originalRequest, exchangeFunction))
                .expectNextCount(1)
                .verifyComplete();

        // Segunda llamada — el filtro delega nuevamente; el cache vive en CaudexTokenService
        StepVerifier.create(filter.filter(originalRequest, exchangeFunction))
                .expectNextCount(1)
                .verifyComplete();

        // fetchToken llamado exactamente 2 veces
        verify(tokenService, times(2)).fetchToken();
    }

    // ── Manejo de errores HTTP de Caudex ──────────────────────────────────────

    @Test
    void filter_http401_throwsCaudexAuthExceptionWith401() {
        ClientRequest originalRequest = ClientRequest.create(
                org.springframework.http.HttpMethod.POST,
                URI.create("https://api.caudex.mx:12443/api/test"))
                .build();

        ClientResponse unauthorizedResponse = mock(ClientResponse.class);
        when(unauthorizedResponse.statusCode()).thenReturn(HttpStatus.UNAUTHORIZED);
        when(unauthorizedResponse.releaseBody()).thenReturn(Mono.empty());
        when(tokenService.fetchToken()).thenReturn(Mono.just("expired-token"));
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(unauthorizedResponse));

        StepVerifier.create(filter.filter(originalRequest, exchangeFunction))
                .expectErrorMatches(ex ->
                        ex instanceof CaudexAuthException cae
                        && cae.getHttpStatus() == HttpStatus.SERVICE_UNAVAILABLE
                        && cae.getUpstreamHttpStatus() == 401)
                .verify();

        verify(tokenService).invalidateCachedToken("Caudex respondió HTTP 401");
    }

    @Test
    void filter_http403_throwsCaudexAuthExceptionWith403() {
        ClientRequest originalRequest = ClientRequest.create(
                org.springframework.http.HttpMethod.POST,
                URI.create("https://api.caudex.mx:12443/api/test"))
                .build();

        ClientResponse forbiddenResponse = mock(ClientResponse.class);
        when(forbiddenResponse.statusCode()).thenReturn(HttpStatus.FORBIDDEN);
        when(forbiddenResponse.releaseBody()).thenReturn(Mono.empty());
        when(tokenService.fetchToken()).thenReturn(Mono.just("valid-token"));
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(forbiddenResponse));

        StepVerifier.create(filter.filter(originalRequest, exchangeFunction))
                .expectErrorMatches(ex ->
                        ex instanceof CaudexAuthException cae
                        && cae.getHttpStatus() == HttpStatus.SERVICE_UNAVAILABLE
                        && cae.getUpstreamHttpStatus() == 403)
                .verify();

        verify(tokenService, never()).invalidateCachedToken(anyString());
    }

    @Test
    void filter_http200_propagatesResponseNormally() {
        ClientRequest originalRequest = ClientRequest.create(
                org.springframework.http.HttpMethod.POST,
                URI.create("https://api.caudex.mx:12443/api/test"))
                .build();

        ClientResponse okResponse = mock(ClientResponse.class);
        when(okResponse.statusCode()).thenReturn(HttpStatus.OK);
        when(tokenService.fetchToken()).thenReturn(Mono.just("valid-token"));
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(okResponse));

        StepVerifier.create(filter.filter(originalRequest, exchangeFunction))
                .assertNext(resp -> assertThat(resp.statusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();
    }

    // ── Propagación de errores del tokenService ───────────────────────────────

    @Test
    void filter_tokenServiceFails_propagatesError() {
        ClientRequest originalRequest = ClientRequest.create(
                org.springframework.http.HttpMethod.POST,
                URI.create("https://api.caudex.mx:12443/api/test"))
                .build();

        when(tokenService.fetchToken()).thenReturn(
                Mono.error(new CaudexAuthException("Error al obtener token OAuth2", 401)));

        StepVerifier.create(filter.filter(originalRequest, exchangeFunction))
                .expectErrorMatches(ex ->
                        ex instanceof CaudexAuthException
                        && ex.getMessage().contains("Error al obtener token OAuth2"))
                .verify();

        verify(exchangeFunction, never()).exchange(any());
    }

    // ── maskToken: logs seguros ───────────────────────────────────────────────

    @Test
    void maskToken_normalToken_showsFirst8CharsOnly() {
        String result = CaudexBearerTokenFilter.maskToken("eyJhbGciOiJSUzI1NiJ9.test.signature");
        assertThat(result).isEqualTo("eyJhbGci...");
        assertThat(result).doesNotContain("test").doesNotContain("signature");
    }

    @Test
    void maskToken_shortToken_returnsAsterisks() {
        assertThat(CaudexBearerTokenFilter.maskToken("short")).isEqualTo("***");
    }

    @Test
    void maskToken_emptyToken_returnsVacio() {
        assertThat(CaudexBearerTokenFilter.maskToken("")).isEqualTo("[vacío]");
        assertThat(CaudexBearerTokenFilter.maskToken(null)).isEqualTo("[vacío]");
        assertThat(CaudexBearerTokenFilter.maskToken("   ")).isEqualTo("[vacío]");
    }

    @Test
    void maskToken_exactlyEightChars_returnsAsterisks() {
        assertThat(CaudexBearerTokenFilter.maskToken("12345678")).isEqualTo("***");
    }
}
