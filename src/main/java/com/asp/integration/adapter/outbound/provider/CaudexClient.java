package com.asp.integration.adapter.outbound.provider;

import com.asp.integration.application.port.outbound.ProviderGateway;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexResponseDto;
import com.asp.integration.domain.exception.CaudexAuthException;
import com.asp.integration.domain.exception.ExternalServiceException;
import com.asp.integration.domain.exception.OperationNotSupportedException;
import com.asp.integration.adapter.outbound.provider.mapper.CaudexMapper;
import com.asp.integration.domain.model.canonical.CanonicalRequest;
import com.asp.integration.domain.model.canonical.CanonicalResponse;
import com.asp.integration.infrastructure.config.properties.CaudexProperties;
import com.asp.integration.shared.constants.OperationTypes;
import com.asp.integration.shared.constants.ProviderConstants;
import com.asp.integration.shared.constants.ResponseCodes;
import com.asp.integration.shared.constants.ResponseMessages;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Outbound Adapter para Caudex Core Banking.
 *
 * Este adapter mantiene sólo las operaciones Caudex requeridas por los contratos
 * oficiales de ASP Pago. Usa el <b>Command Router Pattern</b>:
 *
 * <pre>
 *   operationType → CaudexOperationHandler (endpoint + método HTTP + body builder)
 * </pre>
 *
 * La autenticación OAuth2 (Bearer Token) es completamente transparente:
 * el {@code caudexWebClient} tiene registrado el {@code CaudexBearerTokenFilter}
 * que inyecta un token Caudex cacheado y lo renueva antes de expirar.
 * Este client no conoce nada del flujo OAuth2.
 *
 * Para agregar un nuevo endpoint Caudex:
 * <ol>
 *   <li>Confirmar que existe en los contratos oficiales de ASP Pago.</li>
 *   <li>Registrar en {@code registerHandlers()} con la ruta y el mapper correcto.</li>
 *   <li>Agregar el tipo de operación en {@link com.asp.integration.application.service.InboundAdapterService}.</li>
 *   <li>Crear el DTO de request si tiene campos específicos.</li>
 *   <li>Agregar el método de mapeo en {@link CaudexMapper}.</li>
 * </ol>
 *
 * @autor: HJMB
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaudexClient implements ProviderGateway {

    /**
     * WebClient con CaudexBearerTokenFilter registrado.
     * El filtro inyecta automáticamente Authorization: Bearer {token}.
     */
    @Qualifier("caudexWebClient")
    private final WebClient caudexWebClient;

    private final CaudexMapper mapper;
    private final CaudexProperties caudexProperties;

    /**
     * Registro interno: operationType → handler (endpoint + método + body builder).
     * Inicializado en @PostConstruct.
     */
    private final Map<String, CaudexOperationHandler> operationHandlers = new HashMap<>();

    @PostConstruct
    public void registerHandlers() {

        operationHandlers.put(OperationTypes.CAUDEX_CONSULTA_CUENTA_VISTA,
                handler(endpoint(OperationTypes.CAUDEX_CONSULTA_CUENTA_VISTA), HttpMethod.POST,
                        req -> mapper.toConsultaCuentaVistaRequest(req)));

        operationHandlers.put(OperationTypes.CAUDEX_CONSULTA_SALDOS_VISTA,
                handler(endpoint(OperationTypes.CAUDEX_CONSULTA_SALDOS_VISTA), HttpMethod.POST,
                        req -> mapper.toConsultaSaldosVistaRequest(req)));

        operationHandlers.put(OperationTypes.CAUDEX_DEPOSITO_CUENTA_VISTA,
                handler(endpoint(OperationTypes.CAUDEX_DEPOSITO_CUENTA_VISTA), HttpMethod.POST,
                        req -> mapper.toDepositoCuentaVistaRequest(req)));

        operationHandlers.put(OperationTypes.CAUDEX_RETIRO_CUENTA_VISTA,
                handler(endpoint(OperationTypes.CAUDEX_RETIRO_CUENTA_VISTA), HttpMethod.POST,
                        req -> mapper.toRetiroCuentaVistaRequest(req)));

        operationHandlers.put(OperationTypes.CAUDEX_CONSULTA_HISTORICO_VISTA,
                handler(endpoint(OperationTypes.CAUDEX_CONSULTA_HISTORICO_VISTA), HttpMethod.POST,
                        req -> mapper.toConsultaHistoricoVistaRequest(req)));

        operationHandlers.put(OperationTypes.CAUDEX_CONSULTA_SALDOS_CREDITO,
                handler(endpoint(OperationTypes.CAUDEX_CONSULTA_SALDOS_CREDITO), HttpMethod.POST,
                        req -> mapper.toConsultaSaldosCreditoRequest(req)));

        operationHandlers.put(OperationTypes.CAUDEX_CONSULTA_PERFIL_TRANSACCIONAL,
                handler(endpoint(OperationTypes.CAUDEX_CONSULTA_PERFIL_TRANSACCIONAL), HttpMethod.POST,
                        this::buildFromDatos));

        operationHandlers.put(OperationTypes.CAUDEX_ALTA_RELACION_CLIENTE,
                handler(endpoint(OperationTypes.CAUDEX_ALTA_RELACION_CLIENTE), HttpMethod.POST,
                        this::buildFromDatos));

        log.info("[CAUDEX] {} operaciones registradas desde contratos ASP Pago", operationHandlers.size());
    }

    @Override
    public String providerName() {
        return ProviderConstants.CAUDEX;
    }

    /**
     * Ejecuta la operación Caudex correspondiente al operationType del CanonicalRequest.
     *
     * El Bearer token es inyectado de forma transparente por el CaudexBearerTokenFilter
     * registrado en el caudexWebClient — este método no gestiona autenticación.
     */
    @Override
    @CircuitBreaker(name = "proveedorExterno", fallbackMethod = "fallback")
    @Retry(name = "proveedorExterno")
    @Bulkhead(name = "proveedorExterno", type = Bulkhead.Type.SEMAPHORE)
    public Mono<CanonicalResponse> execute(CanonicalRequest request) {
        String opType = request.getOperationType().toUpperCase();
        log.info("[{}] Ejecutando operación Caudex: {}", request.getCorrelationId(), opType);

        CaudexOperationHandler opHandler = operationHandlers.get(opType);
        if (opHandler == null) {
            return Mono.error(new OperationNotSupportedException(
                    ResponseMessages.OPERACION_CAUDEX_NO_SOPORTADA_PREFIX + opType));
        }

        Object requestBody = opHandler.bodyBuilder().apply(request);

        return executeRequest(opHandler.endpoint(), opHandler.method(), requestBody,
                request.getCorrelationId())
                .map(response -> mapper.toCanonicalResponse(response, request.getCorrelationId()))
                .doOnSuccess(r -> log.info("[{}] Caudex OK resultado={}",
                        request.getCorrelationId(), r.getCodigoResultado()))
                .doOnError(e -> log.error("[{}] Caudex ERROR: {}",
                        request.getCorrelationId(), e.getMessage()));
    }

    /**
     * Fallback activado cuando el circuit breaker está OPEN o se agota el retry.
     * Devuelve una respuesta canónica de error controlado para que el adapter
     * de entrada reciba una respuesta coherente en lugar de una excepción sin procesar.
     */
    public Mono<CanonicalResponse> fallback(CanonicalRequest request, Throwable ex) {
        log.warn("[{}] Caudex FALLBACK activado: {}", request.getCorrelationId(), ex.getMessage());
        return Mono.just(CanonicalResponse.builder()
                .correlationId(request.getCorrelationId())
                .codigoResultado(ResponseCodes.ERROR_PROVEEDOR)
                .mensaje(ResponseMessages.SERVICIO_CAUDEX_NO_DISPONIBLE)
                .proveedor(ProviderConstants.CAUDEX)
                .httpStatus(503)
                .timestamp(Instant.now())
                .build());
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    /**
     * Ejecuta la petición HTTP a Caudex.
     * El Authorization header es añadido por el CaudexBearerTokenFilter antes de
     * que esta petición llegue a la red.
     */
    private Mono<CaudexResponseDto> executeRequest(String endpoint, HttpMethod method,
                                                    Object body, String correlationId) {
        WebClient.RequestBodySpec requestSpec = caudexWebClient
                .method(method)
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Correlation-Id", correlationId);

        if (method == HttpMethod.GET || body == null) {
            return ((WebClient.RequestHeadersSpec<?>) requestSpec)
                    .retrieve()
                    .bodyToMono(CaudexResponseDto.class)
                    .onErrorResume(WebClientResponseException.class, this::handleHttpError);
        }

        return requestSpec
                .bodyValue(body)
                .retrieve()
                .bodyToMono(CaudexResponseDto.class)
                .onErrorResume(WebClientResponseException.class, this::handleHttpError);
    }

    private Mono<CaudexResponseDto> handleHttpError(WebClientResponseException ex) {
        int status = ex.getStatusCode().value();
        String body = ex.getResponseBodyAsString();
        log.error("[CAUDEX] HTTP {} — {}", status, body);

        if (status == 401 || status == 403) {
            return Mono.error(new CaudexAuthException(
                    ResponseMessages.ERROR_AUTENTICACION_CAUDEX_PREFIX + status + ": " + body, status));
        }

        return Mono.error(new ExternalServiceException(
                ResponseCodes.ERROR_SERVICIO_EXTERNO,
                HttpStatus.SERVICE_UNAVAILABLE,
                ResponseMessages.ERROR_TECNICO_CAUDEX,
                ProviderConstants.CAUDEX,
                status
        ));
    }

    /**
     * Pass-through: pasa el mapa datos del CanonicalRequest directamente a Caudex.
     * Usado para operaciones donde el contrato envía el payload Caudex completo
     * dentro de datos sin necesitar un mapper específico.
     */
    private Object buildFromDatos(CanonicalRequest request) {
        return request.getDatos() != null ? request.getDatos() : Map.of();
    }

    private String endpoint(String operationType) {
        return caudexProperties.endpointFor(operationType);
    }

    private CaudexOperationHandler handler(String endpoint, HttpMethod method,
                                            Function<CanonicalRequest, Object> bodyBuilder) {
        return new CaudexOperationHandler(endpoint, method, bodyBuilder);
    }

    /**
     * Descriptor inmutable de una operación Caudex.
     * Encapsula: endpoint Caudex + método HTTP + función que construye el body.
     */
    private record CaudexOperationHandler(
            String endpoint,
            HttpMethod method,
            Function<CanonicalRequest, Object> bodyBuilder
    ) {}
}
