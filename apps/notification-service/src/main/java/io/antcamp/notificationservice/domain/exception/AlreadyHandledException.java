package io.antcamp.notificationservice.domain.exception;

public class AlreadyHandledException extends RuntimeException {
    public AlreadyHandledException(String message) {
        super(message);
    }
}