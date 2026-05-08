package io.antcamp.tradeservice.application.service;

import io.antcamp.tradeservice.infrastructure.dto.AccessTokenResponse;
import io.antcamp.tradeservice.presentation.dto.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TradeService {

    // ── 토큰 ─────────────────────────────────────────────────────────────
    AccessTokenResponse requestAccessToken();
    String requestApprovalKey();
    void clearAll();

    // ── 가격 조회 ─────────────────────────────────────────────────────────
    double getMinutePrice(String stockCode, LocalDateTime dateTime);
    double getNowPrice(String stockCode, LocalDateTime dateTime);
    MinutePriceResponse getPrice(String stockCode, LocalDateTime dateTime);
    StockPriceList stockPriceList(StockList stockList, LocalDateTime dateTime);
    DailyChartResponse getDailyChart(String stockCode, String startDate, String endDate, String periodDivCode);

    // ── 주문 (시장가 / 지정가 통합) ────────────────────────────────────────
    /**
     * 매수/매도 주문.
     * - MARKET : 현재가로 즉시 체결
     * - LIMIT  : 조건 충족 시 즉시 체결, 미충족 시 PENDING 으로 저장
     */
    TradeOrderResponse placeOrder(TradeOrderRequest request, UUID accountId);

    /**
     * 미체결(PENDING) 지정가 주문 취소.
     * 본인 주문이 아닐 경우 예외.
     */
    TradeOrderResponse cancelOrder(UUID tradeId, UUID accountId);

    /**
     * 내 미체결 주문 목록 조회
     */
    List<PendingOrderResponse> getPendingOrders(UUID accountId);

    /**
     * 스케줄러 전용: PENDING 지정가 주문을 현재가와 비교해 체결 처리.
     */
    void executePendingLimitOrders();

    // ── 레거시 (하위 호환) ────────────────────────────────────────────────
    BuyStockResponse buyStock(LocalDateTime time, String stockCode, int stockAmount, UUID accountId);
    SellStockResponse sellStock(LocalDateTime now, String stockCode, int stockAmount, UUID accountId);
}
