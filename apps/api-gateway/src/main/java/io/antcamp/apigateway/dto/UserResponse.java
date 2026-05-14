package io.antcamp.apigateway.dto;

public record UserResponse(
        int status,
        String code,
        String message,
        User data
) {
    public boolean success() {
        return "SUCCESS".equals(code) && data != null;
    }
}