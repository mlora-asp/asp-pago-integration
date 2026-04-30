package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request de depósito a cuenta vista en Caudex.
 * Endpoint: POST /api/depositos-vista/depositoCuenta
 *
 * Body Postman:
 *   { "numeroInstitucion":1, "numeroCuenta":1105, "monto":5,
 *     "referenciaNumerica":987654321, "referenciaAlfanumerica":"alfanumerica",
 *     "idUsuario":"USRCONFIG", "sistemaOperacion":"test" }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexDepositoCuentaVistaRequestDto {

    private Integer numeroInstitucion;
    private Long numeroCuenta;
    private BigDecimal monto;
    private Long referenciaNumerica;
    private String referenciaAlfanumerica;
    private String idUsuario;
    private String sistemaOperacion;
}
