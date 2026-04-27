package io.antcamp.tradeservice.presentation;

import io.antcamp.tradeservice.infrastructure.client.KisWebSocketClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 실시간 체결가 구독 관리 REST API
 *
 * POST   /api/trades/realtime/{stockCode}  — 구독 등록
 * DELETE /api/trades/realtime/{stockCode}  — 구독 해제
 *
 * 구독 후 프론트엔드는 STOMP 로 /topic/price/{stockCode} 를 구독해 실시간 수신.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trades/realtime")
public class RealtimePriceController {

    private final KisWebSocketClient kisWebSocketClient;

    @PostMapping("/{stockCode}")
    public ResponseEntity<String> subscribe(@PathVariable String stockCode) {
        kisWebSocketClient.subscribe(stockCode);
        return ResponseEntity.ok(stockCode + " 실시간 구독 등록 완료");
    }

    @DeleteMapping("/{stockCode}")
    public ResponseEntity<String> unsubscribe(@PathVariable String stockCode) {
        kisWebSocketClient.unsubscribe(stockCode);
        return ResponseEntity.ok(stockCode + " 실시간 구독 해제 완료");
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok(
                kisWebSocketClient.isConnected() ? "KIS WebSocket 연결 중" : "KIS WebSocket 미연결"
        );
    }
}
