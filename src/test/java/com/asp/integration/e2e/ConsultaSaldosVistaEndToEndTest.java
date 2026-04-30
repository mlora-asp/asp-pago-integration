package com.asp.integration.e2e;

import com.asp.integration.infrastructure.audit.AuditLogger;
import com.asp.integration.application.command.ProcessOperationCommand;
import com.asp.integration.adapter.outbound.provider.CaudexClient;
import com.asp.integration.infrastructure.config.caudex.CaudexBearerTokenFilter;
import com.asp.integration.infrastructure.config.caudex.CaudexTokenService;
import com.asp.integration.adapter.inbound.rest.dto.OperacionRequestDto;
import com.asp.integration.adapter.inbound.rest.mapper.CanonicalMapper;
import com.asp.integration.adapter.outbound.provider.mapper.CaudexMapper;
import com.asp.integration.domain.exception.ExternalServiceException;
import com.asp.integration.domain.model.canonical.CanonicalRequest;
import com.asp.integration.domain.model.canonical.CanonicalResponse;
import com.asp.integration.application.service.InboundAdapterService;
import com.asp.integration.application.service.OperationRoutingCatalog;
import com.asp.integration.application.usecase.ProcessOperationUseCase;
import com.asp.integration.application.service.ProviderResolver;
import com.asp.integration.infrastructure.config.properties.CaudexProperties;
import com.asp.integration.testutil.CaudexTestProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test de integración end-to-end para el flujo:
 *
 * <pre>
 *   cliente interno autenticado con JWT vía Kong
 *       → POST /cuentas/saldo
 *       → ProcessOperationUseCase → InboundAdapterService → ProviderResolver → CaudexClient
 *       → CaudexBearerTokenFilter → CaudexTokenService.fetchToken()
 *       → endpoint Caudex configurado para CAUDEX_CONSULTA_SALDOS_VISTA
 *       → CanonicalResponse { codigoResultado: "SUCCESS", proveedor: "CAUDEX" }
 * </pre>
 *
 * Componentes REALES (sin mock):
 * <ul>
 *   <li>{@link InboundAdapterService} — resuelve CAUDEX_CONSULTA_SALDOS_VISTA → "CAUDEX"</li>
 *   <li>{@link ProviderResolver} — selecciona CaudexClient</li>
 *   <li>{@link CaudexClient} — enruta al handler correcto, llama MockWebServer</li>
 *   <li>{@link CaudexBearerTokenFilter} — inyecta Bearer token antes de cada petición</li>
 *   <li>{@link CaudexMapper} — mapeo real CanonicalRequest → CaudexConsultaSaldosVistaRequestDto</li>
 * </ul>
 *
 * Componentes MOCKEADOS:
 * <ul>
 *   <li>{@link CaudexTokenService} — devuelve token fijo (aisla OAuth2 real de Caudex)</li>
 *   <li>{@link CanonicalMapper} — devuelve CanonicalRequest controlado</li>
 *   <li>{@link AuditLogger} — evita efectos secundarios de logging</li>
 * </ul>
 */
@DisplayName("E2E: CAUDEX_CONSULTA_SALDOS_VISTA — flujo completo Kong JWT → Caudex")
@ExtendWith(MockitoExtension.class)
class ConsultaSaldosVistaEndToEndTest {

    // ── Infraestructura de test ───────────────────────────────────────────────

    private MockWebServer mockCaudexServer;

    @Mock
    private CaudexTokenService tokenService;

    @Mock
    private CanonicalMapper canonicalMapper;

    @Mock
    private AuditLogger auditLogger;

    // ── SUT — componentes reales ──────────────────────────────────────────────

    private CaudexClient caudexClient;
    private InboundAdapterService inboundAdapterService;
    private ProviderResolver providerResolver;
    private ProcessOperationUseCase processOperationUseCase;
    private CaudexProperties caudexProperties;

    /** CaudexMapper real — verificamos el mapeo real, no sólo el contrato */
    private final CaudexMapper caudexMapper = Mappers.getMapper(CaudexMapper.class);

    // ── Constantes del test ───────────────────────────────────────────────────

    private static final String SISTEMA_ORIGEN = "kong-jwt-gateway";
    private static final String AUTHENTICATED_CLIENT = "asp-pagos-auth-service";
    private static final String AUTHENTICATED_USER = "user-e2e";
    private static final String AUTHENTICATED_SCOPES = "pagos:consulta";
    private static final String MOCK_TOKEN     = "e2e-bearer-token-abc123";

    // ─────────────────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws IOException {
        mockCaudexServer = new MockWebServer();
        mockCaudexServer.start();

        // CaudexBearerTokenFilter real con tokenService mockeado
        // Este es exactamente el mismo wiring que en producción, pero con un tokenService fake
        CaudexBearerTokenFilter bearerTokenFilter = new CaudexBearerTokenFilter(tokenService);

        WebClient caudexWebClient = WebClient.builder()
                .baseUrl(mockCaudexServer.url("/").toString())
                .filter(bearerTokenFilter)
                .build();

        // CaudexClient recibe endpoints desde configuración; tokenService ya NO está en el client
        caudexProperties = CaudexTestProperties.fromApplicationYaml();
        caudexClient = new CaudexClient(caudexWebClient, caudexMapper, caudexProperties);
        caudexClient.registerHandlers();

        inboundAdapterService = new InboundAdapterService(new OperationRoutingCatalog());
        providerResolver = new ProviderResolver(List.of(caudexClient));

        processOperationUseCase = new ProcessOperationUseCase(
                canonicalMapper, inboundAdapterService, providerResolver, auditLogger);

        // lenient: no todos los tests disparan logSuccess/logError (ej. tests de mapper o de enriquecer)
        lenient().doNothing().when(auditLogger).logSuccess(any(), any());
        lenient().doNothing().when(auditLogger).logError(any(), any());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockCaudexServer.shutdown();
    }

    // ── Helpers del test ──────────────────────────────────────────────────────

    private void givenCanonicalRequestForSaldos(int numeroInstitucion, long numeroCuenta) {
        CanonicalRequest canonical = CanonicalRequest.builder()
                .operationType("CAUDEX_CONSULTA_SALDOS_VISTA")
                .datos(Map.of("numeroInstitucion", numeroInstitucion, "numeroCuenta", numeroCuenta))
                .build();
        when(canonicalMapper.toCanonical(any())).thenReturn(canonical);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test principal: flujo happy path completo
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Happy path: BFF → Bridge → token → consultaSaldos → SUCCESS")
    void consultaSaldosVista_happyPath_returnsSuccessWithSaldo() throws InterruptedException {
        String caudexResponseBody = """
            {
              "datos": {
                "saldoDisponible": 15000.00,
                "saldoContable": 15000.00,
                "saldoRetenido": 0.00
              },
              "mensaje": "Consulta exitosa"
            }
            """;
        mockCaudexServer.enqueue(new MockResponse()
                .setBody(caudexResponseBody)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        when(tokenService.fetchToken()).thenReturn(Mono.just(MOCK_TOKEN));
        givenCanonicalRequestForSaldos(1, 10L);

        OperacionRequestDto monolithRequest = OperacionRequestDto.builder()
                .tipoOperacion("CAUDEX_CONSULTA_SALDOS_VISTA")
                .datos(Map.of("numeroInstitucion", 1, "numeroCuenta", 10))
                .build();

        StepVerifier.create(processOperationUseCase.process(new ProcessOperationCommand(
                        monolithRequest,
                        SISTEMA_ORIGEN,
                        "corr-e2e",
                        AUTHENTICATED_CLIENT,
                        AUTHENTICATED_USER,
                        AUTHENTICATED_SCOPES)))
                .assertNext(response -> {
                    assertThat(response.getCodigoResultado()).isEqualTo("SUCCESS");
                    assertThat(response.getProveedor()).isEqualTo("CAUDEX");
                    assertThat(response.getCorrelationId()).isNotBlank();
                    assertThat(response.getTimestamp()).isNotNull();
                })
                .verifyComplete();

        // Verificar que se llamó al endpoint correcto de Caudex
        RecordedRequest recordedRequest = mockCaudexServer.takeRequest(3, TimeUnit.SECONDS);
        assertThat(recordedRequest).isNotNull();
        assertThat(recordedRequest.getPath())
                .isEqualTo(caudexProperties.endpointFor("CAUDEX_CONSULTA_SALDOS_VISTA"));
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");

        // Verificar que el token fue inyectado por el CaudexBearerTokenFilter
        String authHeader = recordedRequest.getHeader("Authorization");
        assertThat(authHeader).isNotNull();
        assertThat(authHeader).startsWith("Bearer ");
        assertThat(authHeader).contains(MOCK_TOKEN);

        // Verificar que el body contiene los campos esperados
        String requestBody = recordedRequest.getBody().readUtf8();
        assertThat(requestBody).contains("\"numeroInstitucion\"");
        assertThat(requestBody).contains("\"numeroCuenta\"");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Token: el filtro delega en CaudexTokenService por petición.
    // La decisión de cache/refresh vive dentro de CaudexTokenService.
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Filtro Caudex delega a CaudexTokenService por petición")
    void consultaSaldosVista_filterDelegatesTokenResolutionPerRequest() {
        String body = """
            {"datos":{"saldoDisponible":5000.00},"mensaje":"OK"}
            """;
        mockCaudexServer.enqueue(new MockResponse()
                .setBody(body).addHeader("Content-Type", "application/json"));
        mockCaudexServer.enqueue(new MockResponse()
                .setBody(body).addHeader("Content-Type", "application/json"));

        when(tokenService.fetchToken()).thenReturn(Mono.just("token-cacheado-por-servicio"));
        givenCanonicalRequestForSaldos(1, 10L);

        OperacionRequestDto request = OperacionRequestDto.builder()
                .tipoOperacion("CAUDEX_CONSULTA_SALDOS_VISTA")
                .datos(Map.of("numeroInstitucion", 1, "numeroCuenta", 10))
                .build();

        // Primera petición
        StepVerifier.create(processOperationUseCase.process(new ProcessOperationCommand(
                        request,
                        SISTEMA_ORIGEN,
                        "corr-e2e",
                        AUTHENTICATED_CLIENT,
                        AUTHENTICATED_USER,
                        AUTHENTICATED_SCOPES)))
                .assertNext(r -> assertThat(r.getCodigoResultado()).isEqualTo("SUCCESS"))
                .verifyComplete();

        // Configurar mock para segunda llamada
        when(canonicalMapper.toCanonical(any())).thenReturn(
                CanonicalRequest.builder()
                        .operationType("CAUDEX_CONSULTA_SALDOS_VISTA")
                        .datos(Map.of("numeroInstitucion", 1, "numeroCuenta", 10))
                        .build());

        // Segunda petición — el filtro vuelve a delegar resolución de token
        StepVerifier.create(processOperationUseCase.process(new ProcessOperationCommand(
                        request,
                        SISTEMA_ORIGEN,
                        "corr-e2e",
                        AUTHENTICATED_CLIENT,
                        AUTHENTICATED_USER,
                        AUTHENTICATED_SCOPES)))
                .assertNext(r -> assertThat(r.getCodigoResultado()).isEqualTo("SUCCESS"))
                .verifyComplete();

        // El cache real se prueba en CaudexTokenServiceTest; aquí validamos la delegación del filtro.
        verify(tokenService, times(2)).fetchToken();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Error 4xx desde Caudex → RuntimeException propagada al suscriptor
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Error 4xx desde Caudex → RuntimeException propagada (sin AOP en unit test)")
    void consultaSaldosVista_caudex4xxError_propagatesRuntimeException() {
        // En este test unitario CaudexClient se instancia directamente con `new`,
        // por lo que los aspectos Spring AOP (@CircuitBreaker, @Retry) NO están activos.
        // El handler de errores HTTP de CaudexClient convierte 4xx en RuntimeException,
        // que se propaga como onError en la cadena reactiva.
        mockCaudexServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{\"error\":\"cuenta no encontrada\"}")
                .addHeader("Content-Type", "application/json"));

        when(tokenService.fetchToken()).thenReturn(Mono.just(MOCK_TOKEN));
        givenCanonicalRequestForSaldos(1, 99999L);

        OperacionRequestDto request = OperacionRequestDto.builder()
                .tipoOperacion("CAUDEX_CONSULTA_SALDOS_VISTA")
                .datos(Map.of("numeroInstitucion", 1, "numeroCuenta", 99999))
                .build();

        StepVerifier.create(processOperationUseCase.process(new ProcessOperationCommand(
                        request,
                        SISTEMA_ORIGEN,
                        "corr-e2e",
                        AUTHENTICATED_CLIENT,
                        AUTHENTICATED_USER,
                        AUTHENTICATED_SCOPES)))
                .expectErrorMatches(ex ->
                        ex instanceof ExternalServiceException
                        && ex.getMessage().contains("Caudex devolvió un error técnico"))
                .verify();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // InboundAdapterService: resolución de proveedor
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("InboundAdapterService: CAUDEX_CONSULTA_SALDOS_VISTA → proveedor CAUDEX")
    void inboundAdapterService_consultaSaldosVista_resolvesToCaudex() {
        CanonicalRequest canonical = CanonicalRequest.builder()
                .correlationId("corr-enrich-test")
                .operationType("CAUDEX_CONSULTA_SALDOS_VISTA")
                .build();

        inboundAdapterService.enriquecer(canonical);

        assertThat(canonical.getTargetProvider()).isEqualTo("CAUDEX");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CaudexMapper: mapeo de campos con mapper real
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CaudexMapper: toConsultaSaldosVistaRequest mapea numeroInstitucion y numeroCuenta")
    void caudexMapper_toConsultaSaldosVistaRequest_mapsFields() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-mapper-test")
                .datos(Map.of("numeroInstitucion", 1, "numeroCuenta", 10))
                .build();

        var dto = caudexMapper.toConsultaSaldosVistaRequest(request);

        assertThat(dto.getNumeroInstitucion()).isEqualTo(1);
        assertThat(dto.getNumeroCuenta()).isEqualTo(10L);
    }

    @Test
    @DisplayName("CaudexMapper: numeroInstitucion default=1 cuando no se envía")
    void caudexMapper_toConsultaSaldosVistaRequest_defaultInstitucion() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-default-test")
                .datos(Map.of("numeroCuenta", 10))
                .build();

        var dto = caudexMapper.toConsultaSaldosVistaRequest(request);

        assertThat(dto.getNumeroInstitucion()).isEqualTo(1);
        assertThat(dto.getNumeroCuenta()).isEqualTo(10L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CaudexClient: handler registrado + token inyectado por filtro
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CaudexClient: CAUDEX_CONSULTA_SALDOS_VISTA llama endpoint configurado con Bearer")
    void caudexClient_consultaSaldosVista_callsCorrectEndpointWithBearerToken()
            throws InterruptedException {
        mockCaudexServer.enqueue(new MockResponse()
                .setBody("{\"mensaje\":\"OK\",\"datos\":{}}")
                .addHeader("Content-Type", "application/json"));

        when(tokenService.fetchToken()).thenReturn(Mono.just(MOCK_TOKEN));

        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-client-test")
                .operationType("CAUDEX_CONSULTA_SALDOS_VISTA")
                .datos(Map.of("numeroInstitucion", 1, "numeroCuenta", 10))
                .build();

        StepVerifier.create(caudexClient.execute(request))
                .assertNext(r -> {
                    assertThat(r.getProveedor()).isEqualTo("CAUDEX");
                    assertThat(r.getCodigoResultado()).isEqualTo("SUCCESS");
                })
                .verifyComplete();

        RecordedRequest recorded = mockCaudexServer.takeRequest(3, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getPath())
                .isEqualTo(caudexProperties.endpointFor("CAUDEX_CONSULTA_SALDOS_VISTA"));
        assertThat(recorded.getMethod()).isEqualTo("POST");
        // Bearer token inyectado por CaudexBearerTokenFilter
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer " + MOCK_TOKEN);
        assertThat(recorded.getHeader("X-Correlation-Id")).isEqualTo("corr-client-test");
    }
}
