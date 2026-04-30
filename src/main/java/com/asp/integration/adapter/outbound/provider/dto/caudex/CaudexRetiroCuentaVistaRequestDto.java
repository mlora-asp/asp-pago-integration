package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request de retiro de cuenta vista en Caudex.
 * Endpoint: POST /api/depositos-vista/retiroCuenta
 *
 * Body Postman:
 *   { "numeroInstitucion":1, "numeroCuenta":1105, "monto":10,
 *     "referenciaNumerica":987654321, "referenciaAlfanumerica":"REF-ABC-2025",
 *     "numeroCuentaEje":1234, "claveCanalOperacion":1, "instrumentoMonetario":1,
 *     "idUsuario":"USRCONFIG", "sistemaOperacion":"test",
 *     "tipoCuentaEje":1, "fechaAplicacion":"2026-02-26", "montoTransaccion":100 }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexRetiroCuentaVistaRequestDto {

    private Integer numeroInstitucion;
    private Long numeroCuenta;
    private BigDecimal monto;
    private Long referenciaNumerica;
    private String referenciaAlfanumerica;
    private Long numeroCuentaEje;
    private Integer claveCanalOperacion;
    private Integer instrumentoMonetario;
    private String idUsuario;
    private String sistemaOperacion;
    private Integer tipoCuentaEje;
    private String fechaAplicacion;
    private BigDecimal montoTransaccion;
}
