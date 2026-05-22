package com.asp.integration.adapter.outbound.provider;

import com.asp.integration.adapter.outbound.provider.mapper.CaudexMapper;
import com.asp.integration.domain.exception.ExternalServiceException;
import com.asp.integration.domain.model.canonical.CanonicalRequest;
import com.asp.integration.domain.model.canonical.CanonicalResponse;
import com.asp.integration.testutil.CaudexTestProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Pruebas del cliente Caudex.
 *
 * @autor: HJMB
 */
@ExtendWith(MockitoExtension.class)
class CaudexClientTest {

    private MockWebServer mockWebServer;
    private CaudexClient caudexClient;

    @Mock
    private CaudexMapper mapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        ExchangeFilterFunction bearerTokenFilter = ExchangeFilterFunction.ofRequestProcessor(req -> {
            ClientRequest authorizedRequest = ClientRequest.from(req)
                    .header("Authorization", "Bearer test-token-mock")
                    .build();
            return Mono.just(authorizedRequest);
        });

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .filter(bearerTokenFilter)
                .build();

        caudexClient = new CaudexClient(webClient, mapper, CaudexTestProperties.fromApplicationYaml());
        caudexClient.registerHandlers();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    // ── Smoke tests ───────────────────────────────────────────────────────────

    @Test
    void providerName_returnsCaudex() {
        assertThat(caudexClient.providerName()).isEqualTo("CAUDEX");
    }

    @Test
    void fallback_returnsFallbackResponse() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("test-fallback-01")
                .operationType("CAUDEX_CONSULTA_CLIENTE")
                .build();

        StepVerifier.create(caudexClient.fallback(request, new RuntimeException("timeout")))
                .assertNext(resp -> {
                    assertThat(resp.getCodigoResultado()).isEqualTo("ERROR_PROVEEDOR");
                    assertThat(resp.getProveedor()).isEqualTo("CAUDEX");
                    assertThat(resp.getHttpStatus()).isEqualTo(503);
                    assertThat(resp.getCorrelationId()).isEqualTo("test-fallback-01");
                })
                .verifyComplete();
    }

    @Test
    void ejecutar_operacionNoSoportada_returnsError() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("test-unsupported-01")
                .operationType("CAUDEX_OPERACION_INEXISTENTE")
                .datos(Map.of())
                .build();

        StepVerifier.create(caudexClient.execute(request))
                .expectErrorMatches(ex ->
                        ex instanceof com.asp.integration.domain.exception.OperationNotSupportedException
                        && ex.getMessage().contains("CAUDEX_OPERACION_INEXISTENTE"))
                .verify();
    }

    // ── Consulta saldos vista (flujo prioritario asp-pago-management) ─────────

    @Test
    void ejecutar_consultaSaldosVista_returnsCanonicalResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"datos\":{\"saldoDisponible\":5000.00,\"saldoContable\":5000.00}," +
                         "\"mensaje\":\"OK\"}")
                .addHeader("Content-Type", "application/json"));

        CanonicalResponse expectedResponse = CanonicalResponse.builder()
                .correlationId("corr-saldos-01")
                .codigoResultado("SUCCESS")
                .proveedor("CAUDEX")
                .httpStatus(200)
                .build();

        when(mapper.toConsultaSaldosVistaRequest(any())).thenReturn(
                com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexConsultaSaldosVistaRequestDto.builder()
                        .numeroInstitucion(1)
                        .numeroCuenta(10L)
                        .build());
        when(mapper.toCanonicalResponse(any(), anyString())).thenReturn(expectedResponse);

        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-saldos-01")
                .operationType("CAUDEX_CONSULTA_SALDOS_VISTA")
                .datos(Map.of("numeroInstitucion", 1, "numeroCuenta", 10))
                .build();

        StepVerifier.create(caudexClient.execute(request))
                .assertNext(resp -> {
                    assertThat(resp.getCodigoResultado()).isEqualTo("SUCCESS");
                    assertThat(resp.getProveedor()).isEqualTo("CAUDEX");
                    assertThat(resp.getCorrelationId()).isEqualTo("corr-saldos-01");
                })
                .verifyComplete();
    }

    @Test
    void ejecutar_consultaSaldosVista_requestContiensBearerToken() throws InterruptedException {
        // Verifica que el Authorization header fue inyectado por el filtro
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"datos\":{\"saldoDisponible\":1000.00}}")
                .addHeader("Content-Type", "application/json"));

        when(mapper.toConsultaSaldosVistaRequest(any())).thenReturn(
                com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexConsultaSaldosVistaRequestDto.builder()
                        .numeroInstitucion(1).numeroCuenta(10L).build());
        when(mapper.toCanonicalResponse(any(), anyString())).thenReturn(
                CanonicalResponse.builder().codigoResultado("SUCCESS").proveedor("CAUDEX").build());

        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-bearer-check")
                .operationType("CAUDEX_CONSULTA_SALDOS_VISTA")
                .datos(Map.of("numeroInstitucion", 1, "numeroCuenta", 10))
                .build();

        StepVerifier.create(caudexClient.execute(request))
                .assertNext(r -> assertThat(r.getCodigoResultado()).isEqualTo("SUCCESS"))
                .verifyComplete();

        // Verificar que la petición llegó al mock con el Authorization header
        var recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer test-token-mock");
    }

    // ── Depósito cuenta vista ─────────────────────────────────────────────────

    @Test
    void ejecutar_depositoCuentaVista_returnsCanonicalResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"mensaje\":\"Depósito aplicado exitosamente\"}")
                .addHeader("Content-Type", "application/json"));

        when(mapper.toDepositoCuentaVistaRequest(any())).thenReturn(
                com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexDepositoCuentaVistaRequestDto.builder()
                        .numeroInstitucion(1).numeroCuenta(1105L).build());
        when(mapper.toCanonicalResponse(any(), anyString())).thenReturn(
                CanonicalResponse.builder().codigoResultado("SUCCESS").proveedor("CAUDEX").build());

        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("test-deposito-01")
                .operationType("CAUDEX_DEPOSITO_CUENTA_VISTA")
                .datos(Map.of("numeroInstitucion", 1, "numeroCuenta", 1105))
                .build();

        StepVerifier.create(caudexClient.execute(request))
                .assertNext(resp -> assertThat(resp.getCodigoResultado()).isEqualTo("SUCCESS"))
                .verifyComplete();
    }

    @Test
    void ejecutar_onboarding_whenCaudexReturnsBadRequest_propagatesCaudexValidationError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{\"code\":8144,\"message\":null,\"error\":\"El campo 'datosComplementariosPF.regimenFiscal' es un dato requerido\",\"data\":null}")
                .addHeader("Content-Type", "application/json"));

        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-onboarding-400")
                .operationType("CAUDEX_ONBOARDING_ALTA")
                .datos(Map.of("numTipoPersona", 1))
                .build();

        StepVerifier.create(caudexClient.execute(request))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ExternalServiceException.class);
                    ExternalServiceException ex = (ExternalServiceException) error;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getErrorCode()).isEqualTo("CAUDEX_8144");
                    assertThat(ex.getUpstreamStatus()).isEqualTo(400);
                    assertThat(ex.getProvider()).isEqualTo("CAUDEX");
                    assertThat(ex.getMessage()).contains("datosComplementariosPF.regimenFiscal");
                })
                .verify();
    }

}
