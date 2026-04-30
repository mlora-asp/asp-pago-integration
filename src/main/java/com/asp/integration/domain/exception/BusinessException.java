package com.asp.integration.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base para excepciones conocidas del dominio/aplicación que deben traducirse
 * a una respuesta HTTP controlada sin exponer detalles internos.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public BusinessException(String errorCode, HttpStatus httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public BusinessException(String errorCode, HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
