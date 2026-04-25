package io.antcamp.tradeservice.infrastructure.config.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import static io.antcamp.tradeservice.infrastructure.client.KisWebSocketClient.log;


@Component
public class KisWebSocketHandler extends TextWebSocketHandler {
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("연결 수립: sessionId={}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        // PINGPONG 처리 (KIS 서버 연결 유지)
        if (payload.startsWith("0|") || payload.startsWith("1|")) {
            handleRealtimeData(payload);
        } else {
            // JSON 응답 (구독 성공/실패 응답)
            log.info("응답 수신: {}", payload);
        }
    }

    private void handleRealtimeData(String payload) {
        // KIS 실시간 데이터 포맷: "0|tr_id|count|data1^data2^..."
        String[] parts = payload.split("\\|");
        if (parts.length < 4) return;

        String trId = parts[1];
        String data = parts[3];
        String[] fields = data.split("\\^");

        if ("H0STCNT0".equals(trId) && fields.length > 2) {
            String stockCode = fields[0];
            String price     = fields[2];
            log.info("[체결가] 종목={}, 현재가={}", stockCode, price);
            // 이후 비즈니스 로직 처리
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("연결 종료: status={}", status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable ex) {
        log.error("전송 오류: {}", ex.getMessage());
    }
}