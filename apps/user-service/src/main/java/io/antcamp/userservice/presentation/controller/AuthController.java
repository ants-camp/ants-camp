package io.antcamp.userservice.presentation.controller;

import common.dto.CommonResponse;
import io.antcamp.userservice.application.service.AuthService;
import io.antcamp.userservice.presentation.dto.request.LoginRequest;
import io.antcamp.userservice.presentation.dto.request.ReissueRequest;
import io.antcamp.userservice.presentation.dto.response.LoginResponse;
import io.antcamp.userservice.presentation.controller.docs.AuthControllerDocs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return CommonResponse.ok("로그인에 성공했습니다.", response);
    }

    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        authService.logout(userId);
        return CommonResponse.ok("로그아웃에 성공했습니다.", null);
    }

    @PostMapping("/reissue")
    public ResponseEntity<CommonResponse<LoginResponse>> reissue(
            @Valid @RequestBody ReissueRequest request
    ) {
        LoginResponse response = authService.reissue(request);
        return CommonResponse.ok("토큰 재발급에 성공했습니다.", response);
    }
}
