package io.antcamp.apigateway.dto;

import java.util.UUID;

public record User(
        UUID userId,
        String email,
        String name,
        String role,
        String phone,
        String status
) {
}
