package com.asp.integration.infrastructure.exception;

import com.asp.integration.domain.exception.BusinessException;
import com.asp.integration.domain.exception.ExternalServiceException;
import com.asp.integration.shared.constants.ResponseCodes;
import com.asp.integration.shared.constants.ResponseMessages;
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
                        .codigoResultado(ResponseCodes.ERROR_VALIDACION)
                        .mensaje(ResponseMessages.CAMPOS_INVALIDOS)
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
                        .codigoResultado(ResponseCodes.ERROR_INTERNO)
                        .mensaje(ResponseMessages.ERROR_INTERNO_BRIDGE)
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
                case SERVICE_UNAVAILABLE -> ResponseMessages.SERVICIO_EXTERNO_NO_DISPONIBLE;
                default -> ResponseMessages.ERROR_PROCESAR_SOLICITUD;
            };
        }

        if (originalMessage == null || originalMessage.isBlank()) {
            return ResponseMessages.SOLICITUD_NO_PROCESADA;
        }

        if (originalMessage.length() > 300) {
            return originalMessage.substring(0, 300);
        }

        return originalMessage;
    }
}
