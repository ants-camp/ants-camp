package io.antcamp.userservice.presentation.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
}