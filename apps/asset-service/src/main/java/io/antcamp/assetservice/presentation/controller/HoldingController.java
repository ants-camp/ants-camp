package io.antcamp.assetservice.presentation.controller;

import common.dto.ApiResponse;
import io.antcamp.assetservice.application.dto.command.BuyHoldingCommand;
import io.antcamp.assetservice.application.dto.command.SellHoldingCommand;
import io.antcamp.assetservice.application.dto.query.HoldingResult;
import io.antcamp.assetservice.application.service.HoldingService;
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
    public ResponseEntity<Void> buy(@RequestBody BuyHoldingCommand command) {
        holdingService.buy(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sell")
    public ResponseEntity<Void> sell(@RequestBody SellHoldingCommand command) {
        holdingService.sell(command);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HoldingResult>>> getHoldings(
            @RequestParam UUID accountId
    ) {
        List<HoldingResult> result = holdingService.getHoldings(accountId);
        return ApiResponse.ok(result);
    }
}