package io.antcamp.tradeservice.presentation;

import io.antcamp.tradeservice.application.service.StockService;
import io.antcamp.tradeservice.infrastructure.client.KisWebSocketClient;
import io.antcamp.tradeservice.infrastructure.dto.StockSearchResponse;
import io.antcamp.tradeservice.presentation.docs.StockControllerDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 종목 관련 REST API
 *
 * GET    /api/stocks/search?q={keyword}&limit={n}   — 종목 검색
 * POST   /api/stocks/realtime/{stockCode}            — 체결+호가 구독
 * DELETE /api/stocks/realtime/{stockCode}            — 체결+호가 해제
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockController implements StockControllerDocs {

    private final StockService        stockService;
    private final KisWebSocketClient  kisWebSocketClient;

    /**
     * 종목명 또는 종목코드로 검색 (국내주식 전용)
     *
     * @param q     검색어 (비어 있으면 전체 반환)
     * @param limit 최대 결과 수 (기본 20)
     */
    @GetMapping("/search")
    public ResponseEntity<List<StockSearchResponse>> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(stockService.search(q, limit));
    }

    /**
     * 특정 종목의 실시간 체결가(H0STCNT0) + 호가(H0STASP0) 동시 구독 등록
     * 구독 후 프론트엔드는 STOMP 로
     *   /topic/price/{stockCode}     — 체결가
     *   /topic/orderbook/{stockCode} — 호가
     * 를 구독하면 실시간 데이터 수신
     */
    @PostMapping("/realtime/{stockCode}")
    public ResponseEntity<String> subscribe(@PathVariable String stockCode) {
        kisWebSocketClient.subscribe(stockCode);
        kisWebSocketClient.subscribeOrderBook(stockCode);
        return ResponseEntity.ok(stockCode + " 실시간 구독 완료 (체결 + 호가)");
    }

    /** 체결+호가 구독 일괄 해제 */
    @DeleteMapping("/realtime/{stockCode}")
    public ResponseEntity<String> unsubscribe(@PathVariable String stockCode) {
        kisWebSocketClient.unsubscribe(stockCode);
        kisWebSocketClient.unsubscribeOrderBook(stockCode);
        return ResponseEntity.ok(stockCode + " 실시간 구독 해제 완료");
    }
}
