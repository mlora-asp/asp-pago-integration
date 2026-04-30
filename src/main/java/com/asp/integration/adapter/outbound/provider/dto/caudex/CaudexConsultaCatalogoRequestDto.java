package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request de consulta de catálogo en Caudex.
 * Endpoint: POST /api/comunes/consultarCatalogo
 *
 * Body Postman:
 *   { "numeroInstitucion":"1", "claveCatalogo":1 }
 *
 * Nota: numeroInstitucion es String en este endpoint específico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexConsultaCatalogoRequestDto {

    /** String en este endpoint (Caudex inconsistency) */
    private String numeroInstitucion;
    private Integer claveCatalogo;
}
