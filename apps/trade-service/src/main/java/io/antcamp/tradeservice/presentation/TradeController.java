package io.antcamp.tradeservice.presentation;

import common.dto.ApiResponse;
import io.antcamp.tradeservice.application.service.TradeService;
import io.antcamp.tradeservice.infrastructure.annotation.LoginAccount;
import io.antcamp.tradeservice.infrastructure.dto.AccessTokenResponse;
import io.antcamp.tradeservice.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    // ── 토큰 ──────────────────────────────────────────────────────────────

    @PostMapping("/access-token")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> getKisAccessToken() {
        return ApiResponse.ok(tradeService.requestAccessToken());
    }

    // ── 가격 조회 ──────────────────────────────────────────────────────────

    /**
     * GET /api/trades/minute-price?stock_code=005930
     * date_time 생략 시 현재 시각 기준
     */
    @GetMapping("/minute-price")
    public ResponseEntity<ApiResponse<Double>> getMinutePrice(
            @RequestParam("stock_code") String stockCode,
            @RequestParam(value = "date_time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime
    ) {
        return ApiResponse.ok(tradeService.getMinutePrice(stockCode, orNow(dateTime)));
    }

    @GetMapping("/now-price")
    public ResponseEntity<ApiResponse<Double>> getNowPrice(
            @RequestParam("stock_code") String stockCode,
            @RequestParam(value = "date_time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime
    ) {
        return ApiResponse.ok(tradeService.getNowPrice(stockCode, orNow(dateTime)));
    }

    @GetMapping("/price")
    public ResponseEntity<ApiResponse<MinutePriceResponse>> getPrice(
            @RequestParam("stock_code") String stockCode,
            @RequestParam(value = "date_time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime
    ) {
        return ApiResponse.ok(tradeService.getPrice(stockCode, orNow(dateTime)));
    }

    /**
     * GET /api/trades/chart?stock_code=005930&start_date=20240101&end_date=20241231&period=D
     * period: D=일봉 W=주봉 M=월봉 Y=년봉
     */
    @GetMapping("/chart")
    public ResponseEntity<ApiResponse<DailyChartResponse>> getDailyChart(
            @RequestParam("stock_code") String stockCode,
            @RequestParam("start_date") String startDate,
            @RequestParam("end_date")   String endDate,
            @RequestParam(value = "period", defaultValue = "D") String period
    ) {
        return ApiResponse.ok(tradeService.getDailyChart(stockCode, startDate, endDate, period));
    }

    @PostMapping("/stock-price-list")
    public ResponseEntity<ApiResponse<StockPriceList>> stockPriceList(
            @RequestBody StockList stockList,
            @RequestParam(value = "date_time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime
    ) {
        return ApiResponse.ok(tradeService.stockPriceList(stockList, orNow(dateTime)));
    }

    // ── 주문 (시장가 / 지정가 통합) ────────────────────────────────────────

    /**
     * POST /api/trades/order
     *
     * 시장가 매수 예시:
     * { "stockCode": "005930", "stockAmount": 10, "orderType": "MARKET", "side": "BUY" }
     *
     * 지정가 매도 예시:
     * { "stockCode": "005930", "stockAmount": 5, "orderType": "LIMIT", "side": "SELL", "limitPrice": 85000 }
     *
     * 응답 status:
     *   EXECUTED  — 즉시 체결
     *   PENDING   — 조건 미충족, 미체결 대기 (tradeId 포함)
     */
    @PostMapping("/order")
    public ResponseEntity<ApiResponse<TradeOrderResponse>> placeOrder(
            @RequestBody TradeOrderRequest request,
            @LoginAccount UUID accountId
    ) {
        return ApiResponse.ok(tradeService.placeOrder(request, accountId));
    }

    /**
     * DELETE /api/trades/order/{tradeId}
     * 미체결(PENDING) 지정가 주문 취소.
     * 본인 주문만 취소 가능.
     */
    @DeleteMapping("/order/{tradeId}")
    public ResponseEntity<ApiResponse<TradeOrderResponse>> cancelOrder(
            @PathVariable UUID tradeId,
            @LoginAccount UUID accountId
    ) {
        return ApiResponse.ok(tradeService.cancelOrder(tradeId, accountId));
    }

    /**
     * GET /api/trades/pending
     * 내 미체결 주문 목록 조회.
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PendingOrderResponse>>> getPendingOrders(
            @LoginAccount UUID accountId
    ) {
        return ApiResponse.ok(tradeService.getPendingOrders(accountId));
    }

    // ── 레거시 엔드포인트 (하위 호환) ─────────────────────────────────────

    /**
     * @deprecated POST /api/trades/order 로 대체.
     *             기존 클라이언트 호환을 위해 유지.
     */
    @Deprecated
    @PostMapping("/buy")
    public ResponseEntity<ApiResponse<BuyStockResponse>> buyStock(
            @RequestBody BuyStockRequest request,
            @LoginAccount UUID accountId
    ) {
        return ApiResponse.ok(tradeService.buyStock(LocalDateTime.now(),
                request.stockCode(), request.stockAmount(), accountId));
    }

    /**
     * @deprecated POST /api/trades/order 로 대체.
     */
    @Deprecated
    @PostMapping("/sell")
    public ResponseEntity<ApiResponse<SellStockResponse>> sellStock(
            @RequestBody BuyStockRequest request,
            @LoginAccount UUID accountId
    ) {
        return ApiResponse.ok(tradeService.sellStock(LocalDateTime.now(),
                request.stockCode(), request.stockAmount(), accountId));
    }

    // ── 공통 헬퍼 ─────────────────────────────────────────────────────────

    private LocalDateTime orNow(LocalDateTime dateTime) {
        return dateTime != null ? dateTime : LocalDateTime.now();
    }
}
