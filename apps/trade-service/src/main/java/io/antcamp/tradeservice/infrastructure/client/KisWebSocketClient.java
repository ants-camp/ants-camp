package io.antcamp.tradeservice.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.antcamp.tradeservice.application.service.TradeService;
import io.antcamp.tradeservice.infrastructure.config.handler.KisWebSocketHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.Map;

/**
 * KIS WebSocket 연결 관리 클라이언트
 *
 * 책임:
 *  1. 애플리케이션 시작 시 WS 연결 (@PostConstruct)
 *  2. 종목 구독/해제 메시지 전송
 *  3. 애플리케이션 종료 시 WS 연결 해제 (@PreDestroy)
 *
 * 실시간 데이터 수신·파싱·브로드캐스트는 KisWebSocketHandler 가 담당.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KisWebSocketClient {

    @Value("${kis.ws.url}")
    private String wsUrl;

    private final TradeService tradeService;
    private final KisWebSocketHandler kisWebSocketHandler;
    private final ObjectMapper objectMapper;

    private WebSocketSession session;

    // ─────────────────────────────────────────────────────────────────────

    /**
     * 1단계: REST API 로 WebSocket 접속키(approval_key) 발급
     * 2단계: WebSocket 연결
     *
     * @PostConstruct — Spring 컨텍스트 초기화 완료 후 자동 실행
     */
    @PostConstruct
    public void connect() {
        try {
            // KIS: POST /oauth2/Approval → approval_key (REST access_token 과 별개)
            String approvalKey = tradeService.requestApprovalKey();
            log.info("KIS 접속키 발급 완료");

            StandardWebSocketClient client = new StandardWebSocketClient();
            client.execute(kisWebSocketHandler, wsUrl)
                    .whenComplete((wsSession, ex) -> {
                        if (ex != null) {
                            log.error("KIS WebSocket 연결 실패: {}", ex.getMessage(), ex);
                            return;
                        }
                        this.session = wsSession;
                        log.info("KIS WebSocket 연결 성공: {}", wsUrl);

                        // 연결 직후 관심 종목 자동 구독 예시
                        // subscribe("005930");
                    });

        } catch (Exception e) {
            log.error("KIS WebSocket 초기화 실패: {}", e.getMessage(), e);
        }
    }

    // ─── 구독 / 해제 ─────────────────────────────────────────────────────

    /** 국내주식 실시간 체결가 구독 (H0STCNT0) */
    public void subscribe(String stockCode) {
        sendSubscribeMessage(stockCode, "H0STCNT0", "1");
    }

    /** 체결가 구독 해제 */
    public void unsubscribe(String stockCode) {
        sendSubscribeMessage(stockCode, "H0STCNT0", "2");
    }

    /** 국내주식 실시간 호가 구독 (H0STASP0) */
    public void subscribeOrderBook(String stockCode) {
        sendSubscribeMessage(stockCode, "H0STASP0", "1");
    }

    /** 호가 구독 해제 */
    public void unsubscribeOrderBook(String stockCode) {
        sendSubscribeMessage(stockCode, "H0STASP0", "2");
    }

    /**
     * KIS WebSocket 구독 메시지 전송
     *
     * @param stockCode 종목코드
     * @param trId      TR 코드 (H0STCNT0: 체결, H0STASP0: 호가)
     * @param trType    "1": 등록, "2": 해제
     */
    private void sendSubscribeMessage(String stockCode, String trId, String trType) {
        if (!isConnected()) {
            log.warn("WebSocket 미연결 — stockCode={}, trId={}", stockCode, trId);
            return;
        }
        try {
            String approvalKey = tradeService.requestApprovalKey();

            Map<String, Object> request = Map.of(
                    "header", Map.of(
                            "approval_key", approvalKey,
                            "custtype",     "P",
                            "tr_type",      trType,
                            "content-type", "utf-8"
                    ),
                    "body", Map.of(
                            "input", Map.of(
                                    "tr_id",  trId,
                                    "tr_key", stockCode
                            )
                    )
            );

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(request)));
            log.info("구독 {} 완료: {} ({})", "1".equals(trType) ? "등록" : "해제", stockCode, trId);

        } catch (Exception e) {
            log.error("구독 메시지 전송 실패 [stockCode={}, trId={}]: {}", stockCode, trId, e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────

    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    @PreDestroy
    public void disconnect() {
        if (isConnected()) {
            try {
                session.close();
                log.info("KIS WebSocket 정상 종료");
            } catch (Exception e) {
                log.error("WebSocket 종료 실패: {}", e.getMessage());
            }
        }
    }
}
