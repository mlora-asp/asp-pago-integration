package com.asp.integration.infrastructure.exception;

import com.asp.integration.domain.exception.BusinessException;
import com.asp.integration.domain.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador centralizado y estandarizado de excepciones del Bridge.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            fieldErrors.put(field, error.getDefaultMessage());
        });

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .codigoResultado("ERROR_VALIDACION")
                        .mensaje("Campos inválidos en la solicitud")
                        .path(request.getRequestURI())
                        .errores(fieldErrors)
                        .build()
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex,
                                                           HttpServletRequest request) {
        HttpStatus status = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.BAD_REQUEST;
        String sanitizedMessage = sanitizeMessage(ex.getMessage(), status);

        if (ex instanceof ExternalServiceException externalServiceException) {
            log.error("[BRIDGE] Error externo {} status={} upstreamStatus={} mensaje={}",
                    externalServiceException.getProvider(),
                    status.value(),
                    externalServiceException.getUpstreamStatus(),
                    sanitizedMessage);

            return buildResponse(
                    status,
                    ApiErrorResponse.builder()
                            .timestamp(Instant.now())
                            .status(status.value())
                            .error(status.getReasonPhrase())
                            .codigoResultado(ex.getErrorCode())
                            .mensaje(sanitizedMessage)
                            .path(request.getRequestURI())
                            .proveedor(externalServiceException.getProvider())
                            .upstreamStatus(externalServiceException.getUpstreamStatus())
                            .build()
            );
        }

        log.warn("[BRIDGE] Error de negocio status={} code={} mensaje={}",
                status.value(), ex.getErrorCode(), sanitizedMessage);

        return buildResponse(
                status,
                ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .codigoResultado(ex.getErrorCode())
                        .mensaje(sanitizedMessage)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("[BRIDGE] Error inesperado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                        .codigoResultado("ERROR_INTERNO")
                        .mensaje("Error interno del Bridge de Integración")
                        .path(request.getRequestURI())
                        .build()
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, ApiErrorResponse body) {
        return ResponseEntity.status(status).body(body);
    }

    private String sanitizeMessage(String originalMessage, HttpStatus status) {
        if (status.is5xxServerError()) {
            return switch (status) {
                case SERVICE_UNAVAILABLE -> "Servicio externo temporalmente no disponible";
                default -> "Ocurrió un error al procesar la solicitud";
            };
        }

        if (originalMessage == null || originalMessage.isBlank()) {
            return "La solicitud no pudo ser procesada";
        }

        if (originalMessage.length() > 300) {
            return originalMessage.substring(0, 300);
        }

        return originalMessage;
    }
}
