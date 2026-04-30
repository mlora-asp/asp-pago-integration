package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request de consulta de domicilio detalle de cliente en Caudex.
 * Endpoint: POST /api/clientes/datosDomiciliosDetalle/consulta
 *
 * Body Postman:
 *   { "numeroInstitucion": 1, "numeroCliente": 101321, "consecutivoDomicilio": 1 }
 *
 * Diferencia con CAUDEX_CONSULTA_DOMICILIO:
 *  - Este endpoint usa "numeroCliente" (Long) en lugar de "numCliente" (String/Long)
 *  - Incluye "consecutivoDomicilio" para identificar un domicilio específico
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexConsultaDomicilioDetalleRequestDto {

    private Integer numeroInstitucion;
    private Long numeroCliente;
    /** Número consecutivo del domicilio del cliente */
    private Integer consecutivoDomicilio;
}
