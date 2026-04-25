package io.antcamp.tradeservice.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.antcamp.tradeservice.infrastructure.config.handler.KisWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class KisWebSocketClient {

    public static final Logger log = LoggerFactory.getLogger(KisWebSocketClient.class);
    @Value("${KIS_WS_URL}")
    private String wsUrl;

    @Value("${KIS_APP_ACCESS}")
    private String accessToken;

    private WebSocketSession session;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void connect(){
        WebSocketClient client = new StandardWebSocketClient();
        KisWebSocketHandler handler = new KisWebSocketHandler();
        CompletableFuture<WebSocketSession> future = client.execute(handler, wsUrl);

        future.whenComplete((wsSession, ex)-> {
            if (ex != null) {
                log.error("WebSocket 연결 실패: {}", ex.getMessage());
                return;
            }
            this.session = wsSession;
            log.info("WebSocket 연결 성공: {}", wsUrl);

            // 연결 후 실시간 체결가 구독 예시
            subscribeRealtime("005930"); // 삼성전자
        });
    }
    // 실시간 국내주식 체결가 구독
    public void subscribeRealtime(String stockCode) {
        if (session == null || !session.isOpen()) {
            log.warn("WebSocket 세션이 없습니다. connect()를 먼저 호출하세요.");
            return;
        }

        try {
            Map<String, Object> request = Map.of(
                    "header", Map.of(
                            "approval_key", accessToken,
                            "custtype", "P",           // P: 개인, B: 법인
                            "tr_type", "1",            // 1: 등록, 2: 해제
                            "content-type", "utf-8"
                    ),
                    "body", Map.of(
                            "input", Map.of(
                                    "tr_id", "H0STCNT0",   // 국내주식 실시간 체결가
                                    "tr_key", stockCode
                            )
                    )
            );

            String message = objectMapper.writeValueAsString(request);
            session.sendMessage(new TextMessage(message));
            log.info("구독 요청 전송: stockCode={}", stockCode);

        } catch (Exception e) {
            log.error("구독 요청 실패: {}", e.getMessage());
        }
    }

    public void disconnect() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
                log.info("WebSocket 연결 종료");
            } catch (Exception e) {
                log.error("연결 종료 실패: {}", e.getMessage());
            }
        }
    }
}
