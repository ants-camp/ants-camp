package io.antcamp.tradeservice.presentation;

import common.dto.ApiResponse;
import io.antcamp.tradeservice.application.service.TradeService;
import io.antcamp.tradeservice.infrastructure.annotation.LoginAccount;
import io.antcamp.tradeservice.infrastructure.annotation.LoginUser;
import io.antcamp.tradeservice.infrastructure.dto.AccessTokenResponse;
import io.antcamp.tradeservice.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

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
    public ResponseEntity<ApiResponse<Double>> getMinutePrice (
            @RequestParam("stock_code") String stockCode,
            @RequestParam("date_time")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateTime
    ) {
        return ApiResponse.ok(tradeService.getMinutePrice(stockCode, dateTime));
    }

    @PostMapping("/buy")
    public ResponseEntity<ApiResponse<BuyStockResponse>> buyStock(
            @RequestBody String stockCode,
            @RequestBody int stockAmount,
            @LoginAccount UUID accountId
    ){
        return ApiResponse.ok(tradeService.buyStock(LocalDateTime.now(), stockCode, stockAmount, accountId));
    }

    @PostMapping("/stock-price-list")
    public ResponseEntity<ApiResponse<StockPriceList>> stockPriceList(
            @RequestBody StockList stockList,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateTime
    ){
        return ApiResponse.ok(tradeService.stockPriceList(stockList, dateTime));
    }

    @PostMapping("/sell")
    public ResponseEntity<ApiResponse<SellStockResponse>> sellStock(
            @RequestBody String stockCode,
            @RequestBody int stockAmount,
            @LoginAccount UUID accountId
    ){
        return ApiResponse.ok(tradeService.sellStock(LocalDateTime.now(), stockCode, stockAmount, accountId));
    }
}
