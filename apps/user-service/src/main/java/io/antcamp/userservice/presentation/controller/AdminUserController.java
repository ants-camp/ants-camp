package io.antcamp.userservice.presentation.controller;

import common.dto.CommonResponse;
import io.antcamp.userservice.application.service.UserCommandService;
import io.antcamp.userservice.application.service.UserQueryService;
import io.antcamp.userservice.presentation.dto.request.UserRegisterRequest;
import io.antcamp.userservice.presentation.dto.response.UserResponse;
import io.antcamp.userservice.presentation.controller.docs.AdminUserControllerDocs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController implements AdminUserControllerDocs {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @PostMapping("/manager")
    public ResponseEntity<CommonResponse<UserResponse>> createManager(
            @Valid @RequestBody UserRegisterRequest request
    ) {
        UserResponse response = userCommandService.createManager(request);
        return CommonResponse.created("매니저 계정 생성에 성공했습니다.", response);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> response = userQueryService.getAllUsers();
        return CommonResponse.ok("전체 사용자 조회에 성공했습니다.", response);
    }
}