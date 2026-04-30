package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request de envío de correo electrónico a través de Caudex.
 * Endpoint: POST /api/envio-correo-sms/enviarCorreo
 * Acepta HTML en el campo "cuerpo".
 *
 * Body Postman:
 *   { "numeroInstitucion":1, "fecha":"09/01/2026",
 *     "numeroCliente":202345, "numeroCuenta":987654321,
 *     "encabezado":"Asunto", "cuerpo":"<p>mensaje HTML</p>",
 *     "correoReceptor":"destinatario@example.com" }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexEnvioCorreoRequestDto {

    private Integer numeroInstitucion;
    private String fecha;
    /** Opcional */
    private Long numeroCliente;
    /** Opcional */
    private Long numeroCuenta;
    private String encabezado;
    /** Acepta HTML */
    private String cuerpo;
    private String correoReceptor;
}
