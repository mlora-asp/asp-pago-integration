package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request de consulta de movimientos de crédito en Caudex.
 * Endpoint: POST /api/credito/consultaMovimientosCredito
 *
 * Body Postman:
 *   { "numeroInstitucion":1, "numeroCuenta":100011,
 *     "fechaInicio":"2025-01-01", "fechaFin":"2025-12-31" }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexConsultaMovimientosCreditoRequestDto {

    private Integer numeroInstitucion;
    private Long numeroCuenta;
    /** Formato: yyyy-MM-dd */
    private String fechaInicio;
    /** Formato: yyyy-MM-dd */
    private String fechaFin;
}
