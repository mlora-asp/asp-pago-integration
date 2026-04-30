package com.asp.integration.domain.model.canonical;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Modelo canónico de REQUEST.
 * Es el contrato interno del Bridge — ni el BFF ni los proveedores
 * conocen esta clase directamente.
 *
 * @autor: HJMB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanonicalRequest {

    /** Identificador único de la operación (generado por el Bridge) */
    private String correlationId;

    /** Tipo de operación: CONSULTA_CURP, ENVIO_SMS, PAGO, VALIDACION_ID, etc. */
    private String operationType;

    /** Proveedor destino resuelto (se llena en el Provider Resolver) */
    private String targetProvider;

    /** Referencia de negocio del cliente interno origen */
    private String referencia;

    /** Monto en centavos (si aplica) */
    private BigDecimal monto;

    /** Moneda ISO 4217 */
    private String moneda;

    /** Canal de origen: MOVIL, WEB, API, etc. */
    private String canal;

    /** Sistema interno que originó la petición */
    private String sistemaOrigen;

    /** Cliente interno autenticado frente a Kong (ej. asp-pagos-auth-service) */
    private String authenticatedClient;

    /** Usuario final autenticado en el JWT validado por Kong */
    private String authenticatedUser;

    /** Scopes o permisos efectivos propagados por Kong o derivados del JWT */
    private String authenticatedScopes;

    /** Timestamp de creación */
    private Instant timestamp;

    /** Datos adicionales específicos de la operación */
    private Map<String, Object> datos;
}
