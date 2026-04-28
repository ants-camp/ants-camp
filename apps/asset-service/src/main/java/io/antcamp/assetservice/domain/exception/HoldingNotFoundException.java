package io.antcamp.assetservice.domain.exception;

public class HoldingNotFoundException extends RuntimeException {

    public HoldingNotFoundException(String message) {
        super(message);
    }
}