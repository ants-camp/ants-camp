package io.antcamp.assetservice.presentation.controller;

import common.dto.CommonResponse;
import io.antcamp.assetservice.application.dto.command.CreateAccountCommand;
import io.antcamp.assetservice.application.dto.query.AccountResult;
import io.antcamp.assetservice.application.dto.query.BalanceResult;
import io.antcamp.assetservice.application.service.AccountService;
import io.antcamp.assetservice.presentation.dto.response.AccountResponse;
import io.antcamp.assetservice.presentation.dto.response.BalanceResponse;
import io.antcamp.assetservice.presentation.controller.docs.AccountControllerDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController implements AccountControllerDocs {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<CommonResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountCommand command) {
        UUID createdAccountId = accountService.createAccount(command);
        return CommonResponse.created("계좌 생성에 성공했습니다.", AccountResponse.from(createdAccountId));
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<CommonResponse<BalanceResponse>> deposit(
            @PathVariable UUID accountId,
            @RequestParam Long amount) {

        BalanceResult result = accountService.deposit(accountId, amount);
        return CommonResponse.ok("입금에 성공했습니다.", new BalanceResponse(result.accountId(), result.balance()));
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<CommonResponse<BalanceResponse>> withdraw(
            @PathVariable UUID accountId,
            @RequestParam Long amount) {

        BalanceResult result = accountService.withdraw(accountId, amount);
        return CommonResponse.ok("출금에 성공했습니다.", new BalanceResponse(result.accountId(), result.balance()));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<CommonResponse<AccountResult>> getAccount(
            @PathVariable UUID accountId,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        AccountResult result = accountService.getAccount(accountId, userId);

        return CommonResponse.ok(result);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<AccountResult>>> getMyAccounts(
            @RequestHeader("X-User-Id") UUID userId) {
        List<AccountResult> result = accountService.getAccountsByUserId(userId);
        return CommonResponse.ok(result);
    }

}