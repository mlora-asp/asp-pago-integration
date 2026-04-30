package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response genérico de Caudex.
 * Caudex no tiene un envelope estándar documentado en la colección
 * (los responses están vacíos en el Postman). Se usa un wrapper genérico
 * que captura cualquier campo devuelto usando @JsonIgnoreProperties.
 * El campo "resultado" captura el payload completo como Object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaudexResponseDto {

    @JsonProperty("code")
    private Integer codigo;

    @JsonProperty("message")
    private String mensaje;

    @JsonProperty("error")
    private Object error;

    private Long numCliente;
    private Long numeroCuenta;

    @JsonProperty("data")
    private Object datos;

    private Integer httpStatus;
}
