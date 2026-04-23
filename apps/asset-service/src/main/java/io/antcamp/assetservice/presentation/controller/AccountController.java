package io.antcamp.assetservice.presentation.controller;

import io.antcamp.assetservice.application.dto.command.CreateAccountCommand;
import io.antcamp.assetservice.application.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<UUID> createAccount(@Valid @RequestBody CreateAccountCommand command) {

        UUID createdAccountId = accountService.createAccount(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccountId);
    }
}