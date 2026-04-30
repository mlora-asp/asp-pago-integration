package com.asp.integration.domain.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BusinessException {

    public BadRequestException(String errorCode, String message) {
        super(errorCode, HttpStatus.BAD_REQUEST, message);
    }

    public BadRequestException(String errorCode, String message, Throwable cause) {
        super(errorCode, HttpStatus.BAD_REQUEST, message, cause);
    }
}
