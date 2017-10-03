package com.github.spartusch.hfdemo.exception;

import com.netflix.hystrix.exception.HystrixBadRequestException;

/**
 * A simple business exception. Feign clients require an exception to be thrown for HTTP status codes >= 300.
 * An exception extending from HystrixBadRequestException must be provided and thrown if such status codes should
 * not trigger Hystrix fallback/error behaviour. The BusinessRuntimeException can be used for this purpose.
 */
public class BusinessRuntimeException extends HystrixBadRequestException {
    public BusinessRuntimeException(final String message) {
        super(message);
    }

    public BusinessRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
