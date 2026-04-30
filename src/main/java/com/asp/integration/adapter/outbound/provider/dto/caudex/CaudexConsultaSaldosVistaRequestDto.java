package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request de consulta de saldos de cuenta vista en Caudex.
 * Endpoint: POST /api/depositos-vista/consultaSaldos
 * Monolito origen: asp-pago-management
 *
 * Body Postman:
 *   { "numeroInstitucion": 1, "numeroCuenta": 10 }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexConsultaSaldosVistaRequestDto {

    private Integer numeroInstitucion;
    private Long numeroCuenta;
}
