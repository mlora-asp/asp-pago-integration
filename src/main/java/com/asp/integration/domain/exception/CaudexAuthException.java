package com.asp.integration.domain.exception;

import com.asp.integration.shared.constants.ProviderConstants;
import com.asp.integration.shared.constants.ResponseCodes;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando la autenticación con Caudex falla.
 *
 * Casos cubiertos:
 *  - Token ausente o vacío después del flujo OAuth2
 *  - HTTP 401 Unauthorized devuelto por Caudex (token rechazado)
 *  - HTTP 403 Forbidden devuelto por Caudex (sin permisos)
 *  - Error en el endpoint /oauth2/token de Caudex
 *
 * Los canales ASP Pago no conocen esta excepción: el Bridge la captura
 * y devuelve un CanonicalResponse con codigoResultado="ERROR_AUTH_PROVEEDOR".
 *
 *
 * @autor: HJMB
 */
@Getter
public class CaudexAuthException extends ExternalServiceException {

    /** 0 si el error es previo a la llamada HTTP (token ausente/vacío) */
    private final int upstreamHttpStatus;

    public CaudexAuthException(String message) {
        super(ResponseCodes.ERROR_AUTH_PROVEEDOR, HttpStatus.SERVICE_UNAVAILABLE,
                message, ProviderConstants.CAUDEX, null);
        this.upstreamHttpStatus = 0;
    }

    public CaudexAuthException(String message, int httpStatus) {
        super(ResponseCodes.ERROR_AUTH_PROVEEDOR, HttpStatus.SERVICE_UNAVAILABLE,
                message, ProviderConstants.CAUDEX, httpStatus);
        this.upstreamHttpStatus = httpStatus;
    }

    public CaudexAuthException(String message, Throwable cause) {
        super(ResponseCodes.ERROR_AUTH_PROVEEDOR, HttpStatus.SERVICE_UNAVAILABLE,
                message, ProviderConstants.CAUDEX, null, cause);
        this.upstreamHttpStatus = 0;
    }
}
