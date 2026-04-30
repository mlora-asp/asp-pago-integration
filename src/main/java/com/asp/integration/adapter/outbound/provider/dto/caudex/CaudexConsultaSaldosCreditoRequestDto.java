package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request de consulta de saldos de crédito en Caudex.
 * Endpoint: POST /api/credito/consultaSaldosCredito
 *
 * Body Postman:
 *   { "numeroInstitucion":"1", "numeroCuenta":"100011" }
 *
 * Nota: Caudex recibe ambos campos como String en este endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexConsultaSaldosCreditoRequestDto {

    private String numeroInstitucion;
    private String numeroCuenta;
}
