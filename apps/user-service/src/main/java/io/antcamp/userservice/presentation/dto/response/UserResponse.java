package io.antcamp.userservice.presentation.dto.response;

import io.antcamp.userservice.domain.model.User;

import java.util.UUID;

public record UserResponse(
        UUID userId,
        String email,
        String name,
        String role,
        String phone
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getPhone()
        );
    }
}
