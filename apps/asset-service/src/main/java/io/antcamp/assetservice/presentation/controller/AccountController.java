package io.antcamp.assetservice.presentation.controller;

import common.dto.ApiResponse;
import io.antcamp.assetservice.application.dto.command.CreateAccountCommand;
import io.antcamp.assetservice.application.dto.query.AccountResult;
import io.antcamp.assetservice.application.dto.query.BalanceResult;
import io.antcamp.assetservice.application.service.AccountService;
import io.antcamp.assetservice.presentation.dto.response.AccountResponse;
import io.antcamp.assetservice.presentation.dto.response.BalanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountCommand command) {
        UUID createdAccountId = accountService.createAccount(command);
        return ApiResponse.created("계좌 생성에 성공했습니다.", AccountResponse.from(createdAccountId));
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<ApiResponse<BalanceResponse>> deposit(
            @PathVariable UUID accountId,
            @RequestParam Long amount) {

        BalanceResult result = accountService.deposit(accountId, amount);
        return ApiResponse.ok("입금에 성공했습니다.", new BalanceResponse(result.accountId(), result.balance()));
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<ApiResponse<BalanceResponse>> withdraw(
            @PathVariable UUID accountId,
            @RequestParam Long amount) {

        BalanceResult result = accountService.withdraw(accountId, amount);
        return ApiResponse.ok("출금에 성공했습니다.", new BalanceResponse(result.accountId(), result.balance()));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountResult>> getAccount(
            @PathVariable UUID accountId,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        AccountResult result = accountService.getAccount(accountId, userId);

        return ApiResponse.ok(result);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResult>>> getMyAccounts(
            @RequestHeader("X-User-Id") UUID userId) {
        List<AccountResult> result = accountService.getAccountsByUserId(userId);
        return ApiResponse.ok(result);
    }

}