package io.antcamp.notificationservice.domain.exception;

public class InfrastructureServiceException extends RuntimeException {
    public InfrastructureServiceException(String message) {
        super(message);
    }
}