package com.asp.integration.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "OperacionRequest",
    description = "Solicitud de operación enviada por el BFF al Bridge de Integración"
)
public class OperacionRequestDto {

    @NotBlank(message = "tipoOperacion es obligatorio")
    @Schema(
        description = "Tipo de operación a ejecutar. Determina qué proveedor y endpoint se invoca.",
        example = "CAUDEX_CONSULTA_SALDOS_VISTA",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String tipoOperacion;

    @NotBlank(message = "referencia es obligatoria")
    @Schema(
        description = "Referencia de negocio del cliente interno para trazabilidad.",
        example = "TXN-20240417-001",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String referencia;

    @Schema(
        description = "Monto de la operación en la moneda indicada (si aplica).",
        example = "1500.00"
    )
    private BigDecimal monto;

    @Schema(
        description = "Moneda ISO 4217 (si aplica).",
        example = "MXN"
    )
    private String moneda;

    @NotBlank(message = "canal es obligatorio")
    @Schema(
        description = "Canal de origen de la operación.",
        example = "API",
        allowableValues = {"API", "MOVIL", "WEB", "BACKEND"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String canal;

    @Schema(
        description = "Parámetros específicos de la operación. " +
                      "El contenido varía según tipoOperacion. " +
                      "Consultar la documentación de cada operación para los campos requeridos.",
        example = "{\"numeroInstitucion\": 1, \"numeroCuenta\": 1105}"
    )
    private Map<String, Object> datos;
}
