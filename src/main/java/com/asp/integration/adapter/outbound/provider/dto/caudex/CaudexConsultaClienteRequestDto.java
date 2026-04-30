package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request de consulta de datos básicos para Caudex.
 * Soporta búsqueda por número de cliente, CURP o RFC.
 * Caudex usa el mismo endpoint /api/clientes/datosBasicos/consulta
 * para los tres criterios — el campo no nulo determina el criterio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexConsultaClienteRequestDto {

    private Integer numeroInstitucion;
    private Long numCliente;
    private String curp;
    private String rfc;
}
