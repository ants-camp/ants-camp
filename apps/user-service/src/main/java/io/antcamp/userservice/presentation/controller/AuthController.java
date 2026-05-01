package io.antcamp.userservice.presentation.controller;

import common.dto.ApiResponse;
import io.antcamp.userservice.application.service.AuthService;
import io.antcamp.userservice.presentation.dto.request.LoginRequest;
import io.antcamp.userservice.presentation.dto.request.LogoutRequest;
import io.antcamp.userservice.presentation.dto.request.ReissueRequest;
import io.antcamp.userservice.presentation.dto.response.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ApiResponse.ok("로그인에 성공했습니다.", response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request
    ) {
        authService.logout(request);
        return ApiResponse.ok("로그아웃에 성공했습니다.", null);
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<LoginResponse>> reissue(
            @Valid @RequestBody ReissueRequest request
    ) {
        LoginResponse response = authService.reissue(request);
        return ApiResponse.ok("토큰 재발급에 성공했습니다.", response);
    }
}
