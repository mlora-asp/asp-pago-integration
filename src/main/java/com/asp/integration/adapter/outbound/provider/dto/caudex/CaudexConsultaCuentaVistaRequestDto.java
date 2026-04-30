package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request de consulta de cuenta vista en Caudex.
 * Endpoint: POST /api/depositos-vista/consultaDatosBasicos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexConsultaCuentaVistaRequestDto {

    private Integer numeroInstitucion;
    private Long numeroCuenta;
}
