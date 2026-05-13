package io.antcamp.userservice.presentation.controller;

import common.dto.CommonResponse;
import io.antcamp.userservice.application.service.UserCommandService;
import io.antcamp.userservice.application.service.UserQueryService;
import io.antcamp.userservice.presentation.dto.request.UserRegisterRequest;
import io.antcamp.userservice.presentation.dto.request.UserUpdateRequest;
import io.antcamp.userservice.presentation.dto.response.UserResponse;
import io.antcamp.userservice.presentation.controller.docs.UserControllerDocs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @PostMapping("/register")
    public ResponseEntity<CommonResponse<UserResponse>> register(
            @Valid @RequestBody UserRegisterRequest request
    ) {
        UserResponse response = userCommandService.register(request);
        return CommonResponse.created("회원가입에 성공했습니다.", response);
    }

    @GetMapping("/me")
    public ResponseEntity<CommonResponse<UserResponse>> me(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        UserResponse response = userQueryService.getMyInfo(userId);
        return CommonResponse.ok("내 정보 조회에 성공했습니다.", response);
    }

    @PutMapping("/me")
    public ResponseEntity<CommonResponse<UserResponse>> updateMyInfo(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserResponse response = userCommandService.updateMyInfo(userId, request);
        return CommonResponse.ok("회원 정보 수정에 성공했습니다.", response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<CommonResponse<Void>> withdraw(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        userCommandService.withdraw(userId);
        return CommonResponse.ok("회원 탈퇴에 성공했습니다.", null);
    }
}