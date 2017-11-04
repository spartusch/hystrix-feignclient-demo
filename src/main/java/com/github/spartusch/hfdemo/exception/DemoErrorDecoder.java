package com.github.spartusch.hfdemo.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

/**
 * A simple implementation of feign's {@link ErrorDecoder} interface providing business and technical exceptions.
 * Feign clients require exceptions to be thrown for HTTP status codes >= 300 which triggers Hystrix fallback/error
 * behaviour. This error decoder provides {@link BusinessRuntimeException}s for status codes < 500 in order
 * to avoid Hystrix fallback/error behaviour in these cases. It provides {@link TechnicalRuntimeException}s for
 * status codes in the 5xx range and thus triggers Hystrix fallback/error behaviour in these cases.
 *
 * @author Stefan Partusch
 */
public class DemoErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(final String methodKey, final Response response) {
        final HttpStatus httpStatus = HttpStatus.valueOf(response.status());

        if (httpStatus.is5xxServerError()) {
            return new TechnicalRuntimeException(response.reason());
        }

        return new BusinessRuntimeException(response.reason());
    }

}
