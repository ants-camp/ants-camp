package io.antcamp.tradeservice.infrastructure.config.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.antcamp.tradeservice.infrastructure.dto.OrderBookData;
import io.antcamp.tradeservice.infrastructure.dto.StockPriceData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * KIS WebSocket 수신 핸들러
 *
 * 수신 메시지 종류
 *  1) PINGPONG — KIS 서버가 30초마다 전송. "PONG" 으로 응답하지 않으면 연결 끊김.
 *  2) "0|..." / "1|..." — 실시간 데이터 (파이프 구분자)
 *  3) JSON — 구독 성공/실패 응답
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KisWebSocketHandler extends TextWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // ─────────────────────────────────────────────────────────────────────

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("KIS WebSocket 연결 수립: sessionId={}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("KIS 수신 원문: {}", payload);

        // ① PINGPONG — 반드시 "PONG"으로 응답해야 연결 유지됨 (30초 주기)
        if ("PINGPONG".equals(payload.trim())) {
            session.sendMessage(new TextMessage("PONG"));
            log.debug("PINGPONG 응답 완료");
            return;
        }

        // ② 실시간 데이터 (파이프 구분자 포맷)
        if (payload.startsWith("0|") || payload.startsWith("1|")) {
            handleRealtimeData(payload);
            return;
        }

        // ③ JSON — 구독 등록/해제 응답
        handleJsonResponse(payload);
    }

    // ─── 실시간 데이터 파싱 ──────────────────────────────────────────────

    private void handleRealtimeData(String payload) {
        // 포맷: "0|H0STCNT0|001|005930^160001^71900^-800^-1.10^523000^..."
        //        ↑ 0=데이터  ↑tr_id  ↑건수  ↑캐럿(^) 구분 데이터
        String[] parts = payload.split("\\|", 4);
        if (parts.length < 4) {
            log.warn("파싱 실패 — 필드 부족: {}", payload);
            return;
        }

        String trId = parts[1];

        switch (trId) {
            case "H0STCNT0" -> parseAndBroadcastPrice(parts[3]);
            case "H0STASP0" -> parseAndBroadcastOrderBook(parts[3]);
            default         -> log.debug("미처리 tr_id: {}", trId);
        }
    }

    /**
     * H0STCNT0 필드 파싱 및 STOMP 브로드캐스트
     *
     * 필드 순서 (KIS 개발자센터 문서 기준)
     *  [0] 종목코드   [1] 체결시간(HHmmss)
     *  [2] 현재가     [3] 전일대비     [4] 전일대비율
     *  [5] 체결거래량
     */
    private void parseAndBroadcastPrice(String rawData) {
        String[] fields = rawData.split("\\^");
        if (fields.length < 6) {
            log.warn("H0STCNT0 필드 부족 ({}개): {}", fields.length, rawData);
            return;
        }

        try {
            StockPriceData price = new StockPriceData(
                    fields[0],                         // stockCode
                    fields[1],                         // tradeTime (HHmmss)
                    Long.parseLong(fields[2]),          // currentPrice
                    Long.parseLong(fields[3]),          // priceChange
                    new BigDecimal(fields[4]),          // changeRate
                    Long.parseLong(fields[5])           // volume
            );

            log.info("[실시간 체결가] {} | {}원 | {}{}원 ({}%)",
                    price.stockCode(), price.currentPrice(),
                    price.priceChange() >= 0 ? "▲" : "▼",
                    Math.abs(price.priceChange()), price.changeRate());

            // STOMP 브로드캐스트 → 프론트엔드가 /topic/price/{stockCode} 구독
            messagingTemplate.convertAndSend("/topic/price/" + price.stockCode(), price);

        } catch (NumberFormatException e) {
            log.error("숫자 파싱 실패 — rawData={}", rawData, e);
        }
    }

    /**
     * H0STASP0 호가 데이터 파싱 및 STOMP 브로드캐스트
     *
     * 필드 순서 (KIS 개발자센터 문서 기준):
     *  [0]     종목코드
     *  [1]     영업시간 HHmmss
     *  [2]     시간구분코드
     *  [3~12]  매도호가1~10  (ASKP1~ASKP10, 낮은 가격 순)
     *  [13~22] 매수호가1~10  (BIDP1~BIDP10, 높은 가격 순)
     *  [23~32] 매도호가잔량1~10
     *  [33~42] 매수호가잔량1~10
     *  [43]    총매도호가잔량
     *  [44]    총매수호가잔량
     */
    private void parseAndBroadcastOrderBook(String rawData) {
        String[] f = rawData.split("\\^");
        if (f.length < 45) {
            log.warn("H0STASP0 필드 부족 ({}개): {}", f.length, rawData);
            return;
        }
        try {
            String stockCode = f[0];
            String tradeTime = f[1];

            // 매도호가 (asks): index 0 = 1호가 (가장 낮은 매도)
            List<OrderBookData.OrderLevel> asks = new ArrayList<>(10);
            for (int i = 0; i < 10; i++) {
                long price = Long.parseLong(f[3  + i]);
                long qty   = Long.parseLong(f[23 + i]);
                if (price > 0) asks.add(new OrderBookData.OrderLevel(price, qty));
            }

            // 매수호가 (bids): index 0 = 1호가 (가장 높은 매수)
            List<OrderBookData.OrderLevel> bids = new ArrayList<>(10);
            for (int i = 0; i < 10; i++) {
                long price = Long.parseLong(f[13 + i]);
                long qty   = Long.parseLong(f[33 + i]);
                if (price > 0) bids.add(new OrderBookData.OrderLevel(price, qty));
            }

            long totalAskQty = Long.parseLong(f[43]);
            long totalBidQty = Long.parseLong(f[44]);

            OrderBookData orderBook = new OrderBookData(
                    stockCode, tradeTime, asks, bids, totalAskQty, totalBidQty
            );

            log.debug("[호가] {} | 매도잔량={} 매수잔량={}", stockCode, totalAskQty, totalBidQty);

            // STOMP 브로드캐스트 → 프론트엔드가 /topic/orderbook/{stockCode} 구독
            messagingTemplate.convertAndSend("/topic/orderbook/" + stockCode, orderBook);

        } catch (NumberFormatException e) {
            log.error("H0STASP0 숫자 파싱 실패 — rawData={}", rawData, e);
        }
    }

    // ─── JSON 구독 응답 처리 ─────────────────────────────────────────────

    private void handleJsonResponse(String payload) {
        try {
            JsonNode root   = objectMapper.readTree(payload);
            String trId     = root.path("header").path("tr_id").asText();
            String msgCode  = root.path("body").path("msg_cd").asText();
            String msg      = root.path("body").path("msg1").asText();
            log.info("구독 응답 [tr_id={}, msg_cd={}]: {}", trId, msgCode, msg);
        } catch (Exception e) {
            log.warn("JSON 파싱 불가 — payload={}", payload);
        }
    }

    // ─────────────────────────────────────────────────────────────────────

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.warn("KIS WebSocket 연결 종료: status={}", status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable ex) {
        log.error("KIS WebSocket 전송 오류: {}", ex.getMessage());
    }
}
