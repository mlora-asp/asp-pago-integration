package com.asp.integration.infrastructure.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String codigoResultado,
        String mensaje,
        String path,
        String proveedor,
        Integer upstreamStatus,
        Map<String, String> errores
) {
}
