package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request de validación de CURP a través de Caudex.
 * Endpoint: POST /api/clientes/validacionCURP
 *
 * Body Postman:
 *   { "nombre":"Juan", "apellidoPaterno":"García", "apellidoMaterno":"López",
 *     "fecha":"1990-05-15", "sexo":"H", "estadoNacimiento":"CDMX" }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexValidacionCurpRequestDto {

    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    /** Formato: yyyy-MM-dd */
    private String fecha;
    /** H = Hombre, M = Mujer */
    private String sexo;
    private String estadoNacimiento;
}
