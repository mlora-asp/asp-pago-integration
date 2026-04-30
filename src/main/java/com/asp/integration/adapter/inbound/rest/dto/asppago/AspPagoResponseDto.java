package com.asp.integration.adapter.inbound.rest.dto.asppago;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(name = "RespuestaDto")
public class AspPagoResponseDto {

    @Schema(example = "200")
    private Integer code;

    @Schema(example = "Consulta realizada satisfactoriamente")
    private String message;

    @Schema(nullable = true)
    private String error;

    private Object data;
}
