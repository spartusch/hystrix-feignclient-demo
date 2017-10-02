package com.github.spartusch.hfdemo.exception;

public class TechnicalRuntimeException extends RuntimeException {
    public TechnicalRuntimeException(final String message) {
        super(message);
    }

    public TechnicalRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
