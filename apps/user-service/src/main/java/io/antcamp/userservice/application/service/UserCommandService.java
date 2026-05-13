package io.antcamp.userservice.application.service;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.userservice.domain.model.User;
import io.antcamp.userservice.domain.model.enums.RoleType;
import io.antcamp.userservice.domain.model.enums.UserStatus;
import io.antcamp.userservice.domain.repository.RefreshTokenRedisRepository;
import io.antcamp.userservice.domain.repository.UserRepository;
import io.antcamp.userservice.presentation.dto.request.UserRegisterRequest;
import io.antcamp.userservice.presentation.dto.request.UserUpdateRequest;
import io.antcamp.userservice.presentation.dto.response.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public UserResponse register(UserRegisterRequest request) {
        log.info("Registering user with email: {}", request.email());
        validateDuplicateEmail(request.email());

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .phone(request.phone())
                .role(RoleType.PLAYER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    public UserResponse createManager(UserRegisterRequest request) {
        validateDuplicateEmail(request.email());

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .phone(request.phone())
                .role(RoleType.MANAGER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    public UserResponse updateMyInfo(UUID userId, UserUpdateRequest request) {
        User user = getUserEntity(userId);

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_NOT_APPROVED);
        }

        user.updateProfile(request.name(), request.phone());

        if (request.password() != null && !request.password().isBlank()) {
            user.changePassword(passwordEncoder.encode(request.password()));
        }

        return UserResponse.from(user);
    }

    public void withdraw(UUID userId) {
        User user = getUserEntity(userId);

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_NOT_APPROVED);
        }

        user.withdraw();

        refreshTokenRedisRepository.deleteByUserId(userId);
    }

    private User getUserEntity(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }
}