package io.antcamp.userservice.presentation.controller;


import common.dto.CommonResponse;
import io.antcamp.userservice.application.service.UserQueryService;
import io.antcamp.userservice.presentation.dto.response.InternalUserResponse;
import io.antcamp.userservice.presentation.controller.docs.InternalUserControllerDocs;
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
public class InternalUserController implements InternalUserControllerDocs {

    private final UserQueryService userQueryService;


    @GetMapping("/{userId}")
    public ResponseEntity<CommonResponse<InternalUserResponse>> getUser(
            @PathVariable UUID userId
    ) {
        InternalUserResponse response = userQueryService.getInternalUser(userId);
        return CommonResponse.ok("사용자 인증 정보 조회에 성공했습니다.", response);
    }
}