package com.asp.integration.adapter.inbound.rest.dto.asppago;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.asp.integration.shared.constants.ResponseMessages.BENEFICIARIO_REGISTRADO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(name = "RespuestaDtoBeneficiario")
public class AspPagoBeneficiarioResponseDto {

    @Schema(example = "200")
    private Integer code;

    @Schema(example = BENEFICIARIO_REGISTRADO)
    private String message;

    @Schema(nullable = true)
    private String error;

    @Schema(nullable = true)
    private Object data;
}
