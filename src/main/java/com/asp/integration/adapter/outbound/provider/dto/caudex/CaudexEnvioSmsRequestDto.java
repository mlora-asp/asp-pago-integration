package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request de envío de SMS a través de Caudex.
 * Endpoint: POST /api/envio-correo-sms/enviarSMS
 *
 * Body Postman:
 *   { "numeroInstitucion":1, "numeroCliente":202345, "numeroCuenta":987654321,
 *     "fechaMensaje":"11/01/2026", "mensaje":"Mensaje de prueba",
 *     "numeroTelefonico":5527119309 }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexEnvioSmsRequestDto {

    private Integer numeroInstitucion;
    private Long numeroCliente;
    private Long numeroCuenta;
    private String fechaMensaje;
    private String mensaje;
    private Long numeroTelefonico;
}
