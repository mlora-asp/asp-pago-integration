package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request de consulta de movimientos históricos de cuenta vista en Caudex.
 * Endpoint: POST /api/depositos-vista/consultaHistorico
 * (usado tanto para sección 18. CONSULTA MOVIMIENTOS VISTA como 19. DETALLE MOVIMIENTO VISTA)
 *
 * Body Postman:
 *   { "numeroInstitucion":1, "numeroCuenta":10,
 *     "fechaInicio":"2025-01-01", "fechaFin":"2025-06-20" }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexConsultaHistoricoVistaRequestDto {

    private Integer numeroInstitucion;
    private Long numeroCuenta;
    private String fechaInicio;
    private String fechaFin;
}
