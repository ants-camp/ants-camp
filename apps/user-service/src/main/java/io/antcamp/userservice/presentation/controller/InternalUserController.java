package io.antcamp.userservice.presentation.controller;


import io.antcamp.userservice.aplication.service.UserQueryService;
import io.antcamp.userservice.presentation.dto.response.InternalUserResponse;
import lombok.RequiredArgsConstructor;
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
    public InternalUserResponse getUser(
            @PathVariable UUID userId
    ) {
        return userQueryService.getInternalUser(userId);
    }
}
