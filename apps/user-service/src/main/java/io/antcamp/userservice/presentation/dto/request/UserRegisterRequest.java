package io.antcamp.userservice.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserRegisterRequest(

        @NotBlank
        String email,
        @NotBlank
        String name,
        @NotBlank
        String phone
) {
}
