package io.antcamp.userservice.presentation.controller;

import io.antcamp.userservice.aplication.service.UserCommandService;
import io.antcamp.userservice.aplication.service.UserQueryService;
import io.antcamp.userservice.presentation.dto.request.UserRegisterRequest;
import io.antcamp.userservice.presentation.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @PostMapping("/manager")
    public ResponseEntity<UserResponse> createManager(
            @Valid @RequestBody UserRegisterRequest request
    ) {
        return ResponseEntity.ok(userCommandService.createManager(request));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userQueryService.getAllUsers());
    }
}