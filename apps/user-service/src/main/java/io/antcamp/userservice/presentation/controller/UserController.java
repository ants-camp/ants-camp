package io.antcamp.userservice.presentation.controller;

import common.dto.ApiResponse;
import io.antcamp.userservice.aplication.service.UserCommandService;
import io.antcamp.userservice.aplication.service.UserQueryService;
import io.antcamp.userservice.presentation.dto.request.UserRegisterRequest;
import io.antcamp.userservice.presentation.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody UserRegisterRequest request
    ) {
        UserResponse response = userCommandService.register(request);
        return ApiResponse.created("회원가입에 성공했습니다.", response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        UserResponse response = userQueryService.getMyInfo(userId);
        return ApiResponse.ok("내 정보 조회에 성공했습니다.", response);
    }
}