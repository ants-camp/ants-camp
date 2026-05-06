package io.antcamp.tradeservice.infrastructure.exception;

import lombok.Getter;

@Getter
public class KisApiException extends RuntimeException {

    private final String errorCode;
    private final boolean retryable;

    public KisApiException(String errorCode, String message, boolean retryable) {
        super(message);
        this.errorCode = errorCode;
        this.retryable = retryable;
    }

    public KisApiException(String errorCode, String message) {
        this(errorCode, message, false);
    }
}
