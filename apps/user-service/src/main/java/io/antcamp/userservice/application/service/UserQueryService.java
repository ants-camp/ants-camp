package io.antcamp.userservice.application.service;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.userservice.domain.model.User;
import io.antcamp.userservice.domain.model.enums.UserStatus;
import io.antcamp.userservice.domain.repository.UserRepository;
import io.antcamp.userservice.presentation.dto.response.InternalUserResponse;
import io.antcamp.userservice.presentation.dto.response.UserResponse;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;

    public UserResponse getMyInfo(UUID userId) {
        User user = getUserEntity(userId);
        return UserResponse.from(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public InternalUserResponse getInternalUser(UUID userId) {
        User user = getUserEntity(userId);

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_NOT_APPROVED);
        }

        return InternalUserResponse.from(user);
    }

    private User getUserEntity(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}