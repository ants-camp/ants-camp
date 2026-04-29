package io.antcamp.userservice.presentation.dto.response;

import io.antcamp.userservice.domain.model.User;

import java.util.UUID;

public record InternalUserResponse(
        boolean success,
        String code,
        String message,
        UserData data
) {
    public static InternalUserResponse from(User user) {
        return new InternalUserResponse(
                true,
                "SUCCESS",
                "사용자 조회 성공",
                UserData.from(user)
        );
    }

    public record UserData(
            UUID userId,
            String email,
            String name,
            String role,
            String phone,
            String status
    ) {
        public static UserData from(User user) {
            return new UserData(
                    user.getUserId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole().name(),
                    user.getPhone(),
                    user.getStatus().name()
            );
        }
    }
}
