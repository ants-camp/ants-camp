package io.antcamp.apigateway.dto;

public record UserResponse(
        boolean success,
        User data,
        String error
) {
}
