package com.asp.integration.domain.model.canonical;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Modelo canónico de RESPONSE.
 * Los adapters internos trabajan siempre con esta misma estructura,
 * sin importar qué proveedor respondió.
 *
 * @autor: HJMB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanonicalResponse {

    private String correlationId;

    /** Código normalizado: SUCCESS, ERROR_PROVEEDOR, ERROR_TIMEOUT, ERROR_VALIDACION */
    private String codigoResultado;

    /** Mensaje legible para los adapters de entrada */
    private String mensaje;

    /** HTTP status del proveedor (informativo) */
    private Integer httpStatus;

    /** Proveedor que respondió */
    private String proveedor;

    /** Resultado de negocio (payload específico de la operación) */
    private Object resultado;

    /** Timestamp de respuesta */
    private Instant timestamp;
}
