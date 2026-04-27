package io.antcamp.tradeservice.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * 프론트엔드 ↔ 서버 STOMP WebSocket 설정
 *
 * @EnableWebSocket + @EnableWebSocketMessageBroker 동시 사용 금지.
 * → Handler 중복 등록 문제 발생.
 * → KIS 서버 연결(클라이언트 역할)은 KisWebSocketClient 가 직접 처리하므로
 *   여기서는 STOMP 브로커 설정만 담당한다.
 *
 * 흐름:
 *   KIS WebSocket (외부) → KisWebSocketHandler → SimpMessagingTemplate
 *     → /topic/price/{stockCode} → 프론트엔드 STOMP 구독자
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /topic/** 으로 전송된 메시지를 구독자에게 브로드캐스트
        config.enableSimpleBroker("/topic");
        // 클라이언트 → 서버 메시지 prefix (컨트롤러 @MessageMapping 에 사용)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ① React/브라우저용 — SockJS 폴백 지원
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // ② Postman / 테스트용 — 순수 WebSocket (SockJS 없음)
        //    Postman WebSocket 탭에서 ws://localhost:8084/ws-stomp 로 연결
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*");
    }
}
