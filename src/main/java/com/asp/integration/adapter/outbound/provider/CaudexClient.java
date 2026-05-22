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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Adapter de salida para operaciones Caudex.
 *
 * @autor: HJMB
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaudexClient implements ProviderGateway {

    private static final ObjectMapper ERROR_OBJECT_MAPPER = new ObjectMapper();

    @Qualifier("caudexWebClient")
    private final WebClient caudexWebClient;

    private final CaudexMapper mapper;
    private final CaudexProperties caudexProperties;

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

        operationHandlers.put(OperationTypes.CAUDEX_ONBOARDING_ALTA,
                handler(endpoint(OperationTypes.CAUDEX_ONBOARDING_ALTA), HttpMethod.POST,
                        this::buildFromDatos));

        log.info("[CAUDEX] {} operaciones registradas desde contratos ASP Pago", operationHandlers.size());
    }

    @Override
    public String providerName() {
        return ProviderConstants.CAUDEX;
    }

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
     * Respuesta de respaldo para fallas del proveedor.
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

        if (status == 400) {
            return Mono.error(new ExternalServiceException(
                    caudexErrorCode(body),
                    HttpStatus.BAD_REQUEST,
                    caudexErrorMessage(body),
                    ProviderConstants.CAUDEX,
                    status
            ));
        }

        return Mono.error(new ExternalServiceException(
                ResponseCodes.ERROR_SERVICIO_EXTERNO,
                HttpStatus.SERVICE_UNAVAILABLE,
                ResponseMessages.ERROR_TECNICO_CAUDEX,
                ProviderConstants.CAUDEX,
                status
        ));
    }

    private String caudexErrorCode(String body) {
        try {
            JsonNode root = ERROR_OBJECT_MAPPER.readTree(body);
            JsonNode code = root.get("code");
            if (code != null && !code.isNull()) {
                return "CAUDEX_" + code.asText();
            }
        } catch (Exception ex) {
            log.debug("[CAUDEX] No fue posible extraer code del error 400: {}", ex.getMessage());
        }
        return ResponseCodes.ERROR_SERVICIO_EXTERNO;
    }

    private String caudexErrorMessage(String body) {
        try {
            JsonNode root = ERROR_OBJECT_MAPPER.readTree(body);
            JsonNode error = root.get("error");
            if (error != null && error.isTextual() && !error.asText().isBlank()) {
                return error.asText();
            }

            JsonNode message = root.get("message");
            if (message != null && message.isTextual() && !message.asText().isBlank()) {
                return message.asText();
            }
        } catch (Exception ex) {
            log.debug("[CAUDEX] No fue posible extraer mensaje del error 400: {}", ex.getMessage());
        }
        return ResponseMessages.ERROR_TECNICO_CAUDEX;
    }

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

    private record CaudexOperationHandler(
            String endpoint,
            HttpMethod method,
            Function<CanonicalRequest, Object> bodyBuilder
    ) {}
}
