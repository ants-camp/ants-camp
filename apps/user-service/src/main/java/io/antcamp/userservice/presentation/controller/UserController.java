package io.antcamp.userservice.presentation.controller;

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
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserRegisterRequest request
    ) {
        return ResponseEntity.ok(userCommandService.register(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        return ResponseEntity.ok(userQueryService.getMyInfo(userId));
    }
}