package com.asp.integration.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExternalServiceException extends BusinessException {

    private final String provider;
    private final Integer upstreamStatus;

    public ExternalServiceException(String errorCode, HttpStatus httpStatus, String message,
                                    String provider, Integer upstreamStatus) {
        super(errorCode, httpStatus, message);
        this.provider = provider;
        this.upstreamStatus = upstreamStatus;
    }

    public ExternalServiceException(String errorCode, HttpStatus httpStatus, String message,
                                    String provider, Integer upstreamStatus, Throwable cause) {
        super(errorCode, httpStatus, message, cause);
        this.provider = provider;
        this.upstreamStatus = upstreamStatus;
    }
}
