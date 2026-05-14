package io.antcamp.notificationservice.domain.repository;

import io.antcamp.notificationservice.domain.exception.InvalidInputException;

public record PagingRequest(int page, int size) {
    public PagingRequest {
        if (page < 0) throw new InvalidInputException();
        if (size <= 0) throw new InvalidInputException();
    }
}