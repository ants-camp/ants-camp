package io.antcamp.tradeservice.presentation;

import common.dto.ApiResponse;
import io.antcamp.tradeservice.application.service.TradeService;
import io.antcamp.tradeservice.infrastructure.dto.AccessTokenResponse;
import io.antcamp.tradeservice.presentation.dto.KisAccessToken;
import io.antcamp.tradeservice.presentation.dto.MinutePriceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    @PostMapping("/access-token")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> getKisAccessToken(){
        return ApiResponse.ok(tradeService.requestAccessToken());
    }

    // GET /api/trades/minute-price?stock_code=005930&date_time=2024-01-01T09:00:00
    @GetMapping("/minute-price")
    public ResponseEntity<ApiResponse<MinutePriceResponse>> getMinutePrice (
            @RequestParam("stock_code") String stockCode,
            @RequestParam("date_time")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateTime) {
        return ApiResponse.ok(tradeService.getMinutePrice(stockCode, dateTime));
    }
}
