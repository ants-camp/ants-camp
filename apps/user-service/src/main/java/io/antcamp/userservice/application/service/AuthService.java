package io.antcamp.userservice.application.service;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.userservice.domain.model.JwtProvider;
import io.antcamp.userservice.domain.model.RefreshToken;
import io.antcamp.userservice.domain.model.User;
import io.antcamp.userservice.domain.repository.RefreshTokenRepository;
import io.antcamp.userservice.domain.repository.UserRepository;
import io.antcamp.userservice.presentation.dto.request.LoginRequest;
import io.antcamp.userservice.presentation.dto.request.LogoutRequest;
import io.antcamp.userservice.presentation.dto.request.ReissueRequest;
import io.antcamp.userservice.presentation.dto.response.LoginResponse;
import io.antcamp.userservice.presentation.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_NOT_APPROVED);
        }

        String accessToken = jwtProvider.createAccessToken(user);
        String refreshToken = jwtProvider.createRefreshToken(user);
        Instant refreshTokenExpiresAt = jwtProvider.getExpiration(refreshToken);

        refreshTokenRepository.findByUserId(user.getUserId())
                .ifPresentOrElse(
                        savedToken -> savedToken.updateToken(refreshToken, refreshTokenExpiresAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .userId(user.getUserId())
                                        .token(refreshToken)
                                        .expiresAt(refreshTokenExpiresAt)
                                        .build()
                        )
                );

        return new LoginResponse(
                accessToken,
                refreshToken,
                UserResponse.from(user)
        );
    }

    public void logout(LogoutRequest request) {
        if (!jwtProvider.validateToken(request.refreshToken())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        refreshTokenRepository.deleteByToken(request.refreshToken());
    }

    public LoginResponse reissue(ReissueRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        RefreshToken savedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (savedRefreshToken.isExpired()) {
            refreshTokenRepository.delete(savedRefreshToken);
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }

        UUID userId = jwtProvider.getUserId(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_NOT_APPROVED);
        }

        String newAccessToken = jwtProvider.createAccessToken(user);
        String newRefreshToken = jwtProvider.createRefreshToken(user);
        Instant newRefreshTokenExpiresAt = jwtProvider.getExpiration(newRefreshToken);

        savedRefreshToken.updateToken(newRefreshToken, newRefreshTokenExpiresAt);

        return new LoginResponse(
                newAccessToken,
                newRefreshToken,
                UserResponse.from(user)
        );
    }
}
