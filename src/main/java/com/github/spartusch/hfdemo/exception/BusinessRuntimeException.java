package com.github.spartusch.hfdemo.exception;

import com.netflix.hystrix.exception.HystrixBadRequestException;

public class BusinessRuntimeException extends HystrixBadRequestException {
    public BusinessRuntimeException(final String message) {
        super(message);
    }

    public BusinessRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
