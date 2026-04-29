package io.antcamp.userservice.presentation.dto.response;

import io.antcamp.userservice.domain.model.User;

import java.util.UUID;

public record InternalUserResponse(
        UUID userId,
        String email,
        String name,
        String role,
        String phone,
        String status
) {
    public static InternalUserResponse from(User user) {
        return new InternalUserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getPhone(),
                user.getStatus().name()
        );
    }
}
