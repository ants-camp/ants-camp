package io.antcamp.tradeservice.presentation;

import common.dto.CommonResponse;
import io.antcamp.tradeservice.application.service.TradeService;
import io.antcamp.tradeservice.infrastructure.annotation.LoginAccount;
import io.antcamp.tradeservice.infrastructure.dto.AccessTokenResponse;
import io.antcamp.tradeservice.presentation.docs.TradeControllerDocs;
import io.antcamp.tradeservice.presentation.dto.BuyStockRequest;
import io.antcamp.tradeservice.presentation.dto.BuyStockResponse;
import io.antcamp.tradeservice.presentation.dto.DailyChartResponse;
import io.antcamp.tradeservice.presentation.dto.MinutePriceResponse;
import io.antcamp.tradeservice.presentation.dto.PendingOrderResponse;
import io.antcamp.tradeservice.presentation.dto.SellStockResponse;
import io.antcamp.tradeservice.presentation.dto.StockList;
import io.antcamp.tradeservice.presentation.dto.StockPriceList;
import io.antcamp.tradeservice.presentation.dto.TradeOrderRequest;
import io.antcamp.tradeservice.presentation.dto.TradeOrderResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trades")
public class TradeController implements TradeControllerDocs {

    private final TradeService tradeService;

    // ── 토큰 ──────────────────────────────────────────────────────────────

    @PostMapping("/access-token")
    public ResponseEntity<CommonResponse<AccessTokenResponse>> getKisAccessToken() {
        return CommonResponse.ok(tradeService.requestAccessToken());
    }

    // ── 가격 조회 ──────────────────────────────────────────────────────────

    /**
     * GET /api/trades/minute-price?stock_code=005930 date_time 생략 시 현재 시각 기준
     */
    @GetMapping("/minute-price")
    public ResponseEntity<CommonResponse<Double>> getMinutePrice(
            @RequestParam("stock_code") String stockCode,
            @RequestParam(value = "date_time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime
    ) {
        return CommonResponse.ok(tradeService.getMinutePrice(stockCode, orNow(dateTime)));
    }

    @GetMapping("/now-price")
    public ResponseEntity<CommonResponse<Double>> getNowPrice(
            @RequestParam("stock_code") String stockCode,
            @RequestParam(value = "date_time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime
    ) {
        return CommonResponse.ok(tradeService.getNowPrice(stockCode, orNow(dateTime)));
    }

    @GetMapping("/price")
    public ResponseEntity<CommonResponse<MinutePriceResponse>> getPrice(
            @RequestParam("stock_code") String stockCode,
            @RequestParam(value = "date_time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime
    ) {
        return CommonResponse.ok(tradeService.getPrice(stockCode, orNow(dateTime)));
    }

    /**
     * GET /api/trades/chart?stock_code=005930&start_date=20240101&end_date=20241231&period=D period: D=일봉 W=주봉 M=월봉
     * Y=년봉
     */
    @GetMapping("/chart")
    public ResponseEntity<CommonResponse<DailyChartResponse>> getDailyChart(
            @RequestParam("stock_code") String stockCode,
            @RequestParam("start_date") String startDate,
            @RequestParam("end_date") String endDate,
            @RequestParam(value = "period", defaultValue = "D") String period
    ) {
        return CommonResponse.ok(tradeService.getDailyChart(stockCode, startDate, endDate, period));
    }

    @PostMapping("/stock-price-list")
    public ResponseEntity<CommonResponse<StockPriceList>> stockPriceList(
            @RequestBody StockList stockList,
            @RequestParam(value = "date_time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime
    ) {
        return CommonResponse.ok(tradeService.stockPriceList(stockList, orNow(dateTime)));
    }

    // ── 주문 (시장가 / 지정가 통합) ────────────────────────────────────────

    /**
     * POST /api/trades/order
     * <p>
     * 시장가 매수 예시: { "stockCode": "005930", "stockAmount": 10, "orderType": "MARKET", "side": "BUY" }
     * <p>
     * 지정가 매도 예시: { "stockCode": "005930", "stockAmount": 5, "orderType": "LIMIT", "side": "SELL", "limitPrice": 85000
     * }
     * <p>
     * 응답 status: EXECUTED  — 즉시 체결 PENDING   — 조건 미충족, 미체결 대기 (tradeId 포함)
     */
    // 수정 전:
    // public ResponseEntity<CommonResponse<TradeOrderResponse>> placeOrder(
    //         @RequestBody TradeOrderRequest request) {
    //     return CommonResponse.ok(tradeService.placeOrder(request));
    // }
    @PostMapping("/order")
    public ResponseEntity<CommonResponse<TradeOrderResponse>> placeOrder(
            @RequestBody TradeOrderRequest request,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        return CommonResponse.ok(tradeService.placeOrder(request, userId));
    }

    /**
     * DELETE /api/trades/order/{tradeId} 미체결(PENDING) 지정가 주문 취소. 본인 주문만 취소 가능.
     */
    @DeleteMapping("/order/{tradeId}")
    public ResponseEntity<CommonResponse<TradeOrderResponse>> cancelOrder(
            @PathVariable UUID tradeId,
            @LoginAccount UUID accountId
    ) {
        return CommonResponse.ok(tradeService.cancelOrder(tradeId, accountId));
    }

    /**
     * GET /api/trades/pending 내 미체결 주문 목록 조회.
     */
    @GetMapping("/pending")
    public ResponseEntity<CommonResponse<List<PendingOrderResponse>>> getPendingOrders(
            @LoginAccount UUID accountId
    ) {
        return CommonResponse.ok(tradeService.getPendingOrders(accountId));
    }

    // ── 레거시 엔드포인트 (하위 호환) ─────────────────────────────────────

    /**
     * @deprecated POST /api/trades/order 로 대체. 기존 클라이언트 호환을 위해 유지.
     */
    @Deprecated
    @PostMapping("/buy")
    public ResponseEntity<CommonResponse<BuyStockResponse>> buyStock(
            @RequestBody BuyStockRequest request,
            @LoginAccount UUID accountId,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        // 수정 전: tradeService.buyStock(..., accountId)
        return CommonResponse.ok(tradeService.buyStock(LocalDateTime.now(),
                request.stockCode(), request.stockAmount(), accountId, userId));
    }

    /**
     * @deprecated POST /api/trades/order 로 대체.
     */
    @Deprecated
    @PostMapping("/sell")
    public ResponseEntity<CommonResponse<SellStockResponse>> sellStock(
            @RequestBody BuyStockRequest request,
            @LoginAccount UUID accountId,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        // 수정 전: tradeService.sellStock(..., accountId)
        return CommonResponse.ok(tradeService.sellStock(LocalDateTime.now(),
                request.stockCode(), request.stockAmount(), accountId, userId));
    }

    // ── 공통 헬퍼 ─────────────────────────────────────────────────────────

    private LocalDateTime orNow(LocalDateTime dateTime) {
        return dateTime != null ? dateTime : LocalDateTime.now();
    }
}
