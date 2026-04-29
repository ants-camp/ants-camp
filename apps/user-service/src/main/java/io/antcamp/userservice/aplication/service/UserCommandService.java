package io.antcamp.userservice.aplication.service;

import io.antcamp.userservice.domain.model.User;
import io.antcamp.userservice.domain.model.enums.RoleType;
import io.antcamp.userservice.domain.model.enums.UserStatus;
import io.antcamp.userservice.domain.repository.UserRepository;
import io.antcamp.userservice.presentation.dto.request.UserRegisterRequest;
import io.antcamp.userservice.presentation.dto.response.UserResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;

    public UserResponse register(UserRegisterRequest request) {
        validateDuplicateEmail(request.email());

        User user = User.builder()
                .email(request.email())
                .name(request.name())
                .phone(request.phone())
                .role(RoleType.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    public UserResponse createManager(UserRegisterRequest request) {
        validateDuplicateEmail(request.email());

        User user = User.builder()
                .email(request.email())
                .name(request.name())
                .phone(request.phone())
                .role(RoleType.MANAGER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
    }
}
