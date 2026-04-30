package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request de consulta de buró de crédito en Caudex (PFAE y PM).
 * Endpoint: POST /api/buro-credito/consulta/consultaPM
 *
 * Body Postman:
 *   { "numeroInstitucion":"1", "numCliente":"101315",
 *     "macAddress":"", "fechaConsulta":"2026-02-23", "idUsuario":0 }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexConsultaBuroCreditoRequestDto {

    private String numeroInstitucion;
    private String numCliente;
    private String macAddress;
    private String fechaConsulta;
    private Integer idUsuario;
}
