package io.antcamp.assetservice.presentation.controller;

import common.dto.ApiResponse;
import io.antcamp.assetservice.application.dto.command.BuyHoldingCommand;
import io.antcamp.assetservice.application.dto.command.SellHoldingCommand;
import io.antcamp.assetservice.application.dto.query.HoldingResult;
import io.antcamp.assetservice.application.dto.query.TradeResult;
import io.antcamp.assetservice.application.service.HoldingService;
import io.antcamp.assetservice.presentation.dto.response.HoldingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/holdings")
@RequiredArgsConstructor
public class HoldingController {

    private final HoldingService holdingService;

    @PostMapping("/buy")
    public ResponseEntity<TradeResult> buy(@Valid @RequestBody BuyHoldingCommand command) {
        TradeResult result = holdingService.buy(command);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sell")
    public ResponseEntity<TradeResult> sell(@Valid @RequestBody SellHoldingCommand command) {
        TradeResult result = holdingService.sell(command);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HoldingResponse>>> getHoldings(
            @RequestParam UUID accountId,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        List<HoldingResponse> response = holdingService.getHoldings(accountId, userId)
                .stream()
                .map(HoldingResponse::from)
                .toList();

        return ApiResponse.ok(response);
    }
}