package io.antcamp.tradeservice.presentation;

import io.antcamp.tradeservice.infrastructure.client.KisWebSocketClient;
import io.antcamp.tradeservice.infrastructure.config.SubscriptionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.Set;

/**
 * STOMP 기반 실시간 구독 컨트롤러
 *
 * 클라이언트 → 서버 메시지 경로:
 *   /app/stock/subscribe    — 종목 구독 요청
 *   /app/stock/unsubscribe  — 종목 구독 해제
 *
 * 메시지 바디 예시: { "stockCode": "005930" }
 *
 * 처리 흐름:
 *  1. SubscriptionRegistry 에 세션/종목 등록
 *  2. 해당 종목의 첫 구독자라면 KisWebSocketClient 를 통해 KIS 에 구독 등록
 *  3. KIS → KisWebSocketHandler → /topic/price/{code}, /topic/orderbook/{code} 브로드캐스트
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class RealtimeStompController {

    private final SubscriptionRegistry    registry;
    private final KisWebSocketClient      kisWebSocketClient;
    private final SimpMessagingTemplate   messagingTemplate;

    // ── 구독 등록 ─────────────────────────────────────────────────────────

    @MessageMapping("/stock/subscribe")
    public void subscribe(
            @Payload Map<String, String> payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String sessionId = headerAccessor.getSessionId();
        String stockCode = payload.get("stockCode");

        if (stockCode == null || stockCode.isBlank()) {
            log.warn("[STOMP] subscribe: stockCode 누락 — session={}", sessionId);
            return;
        }

        boolean isFirst = registry.subscribe(sessionId, stockCode);

        if (isFirst) {
            // KIS 에 첫 구독 요청 (체결가 + 호가)
            kisWebSocketClient.subscribe(stockCode);
            kisWebSocketClient.subscribeOrderBook(stockCode);
            log.info("[STOMP] KIS 구독 요청: stockCode={}", stockCode);
        }

        // 구독 확인 메시지를 해당 사용자에게만 전송
        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/subscriptions",
                Map.of(
                        "action",      "subscribed",
                        "stockCode",   stockCode,
                        "subscribers", registry.subscriberCount(stockCode)
                )
        );
    }

    // ── 구독 해제 ─────────────────────────────────────────────────────────

    @MessageMapping("/stock/unsubscribe")
    public void unsubscribe(
            @Payload Map<String, String> payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String sessionId = headerAccessor.getSessionId();
        String stockCode = payload.get("stockCode");

        if (stockCode == null || stockCode.isBlank()) {
            log.warn("[STOMP] unsubscribe: stockCode 누락 — session={}", sessionId);
            return;
        }

        boolean isLast = registry.unsubscribe(sessionId, stockCode);

        if (isLast) {
            // 마지막 구독자가 해제 → KIS 구독 취소
            kisWebSocketClient.unsubscribe(stockCode);
            kisWebSocketClient.unsubscribeOrderBook(stockCode);
            log.info("[STOMP] KIS 구독 해제: stockCode={}", stockCode);
        }
    }

    // ── 세션 종료 이벤트 ─────────────────────────────────────────────────

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        Set<String> emptiedStocks = registry.removeSession(sessionId);

        // 마지막 구독자 없어진 종목 KIS 해제
        for (String stockCode : emptiedStocks) {
            kisWebSocketClient.unsubscribe(stockCode);
            kisWebSocketClient.unsubscribeOrderBook(stockCode);
            log.info("[STOMP] 세션 종료로 KIS 구독 해제: stockCode={}", stockCode);
        }
    }
}
