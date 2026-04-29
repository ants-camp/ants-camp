package io.antcamp.userservice.presentation.controller;


import common.dto.ApiResponse;
import io.antcamp.userservice.aplication.service.UserQueryService;
import io.antcamp.userservice.presentation.dto.response.InternalUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserQueryService userQueryService;


    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<InternalUserResponse>> getUser(
            @PathVariable UUID userId
    ) {
        InternalUserResponse response = userQueryService.getInternalUser(userId);
        return ApiResponse.ok("사용자 인증 정보 조회에 성공했습니다.", response);
    }
}