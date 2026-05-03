package io.antcamp.tradeservice.presentation;

import io.antcamp.tradeservice.application.service.StockSearchService;
import io.antcamp.tradeservice.infrastructure.client.KisWebSocketClient;
import io.antcamp.tradeservice.infrastructure.dto.StockSearchResponse;
import io.antcamp.tradeservice.infrastructure.initializer.StockExcelImporter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockController {

    private final StockSearchService  stockSearchService;
    private final KisWebSocketClient  kisWebSocketClient;
    private final StockExcelImporter  stockExcelImporter;

    /**
     * 종목 검색 (Elasticsearch Fuzzy + Nori + Ngram 복합 쿼리)
     * GET /api/stocks/search?q=삼성&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<List<StockSearchResponse>> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(stockSearchService.search(q, size));
    }

    /**
     * KRX Excel → PostgreSQL 임포트 (관리자)
     * POST /api/stocks/import
     */
    @PostMapping("/import")
    public ResponseEntity<String> importFromExcel() {
        stockExcelImporter.importIfEmpty();
        return ResponseEntity.ok("KRX 종목 데이터 DB 임포트 완료");
    }

    /**
     * PostgreSQL → Elasticsearch 전체 재인덱싱 (관리자)
     * POST /api/stocks/reindex
     */
    @PostMapping("/reindex")
    public ResponseEntity<String> reindex() {
        stockSearchService.reindex();
        return ResponseEntity.ok("ES 종목 인덱스 재구성 완료");
    }

    /** 실시간 체결+호가 구독 */
    @PostMapping("/realtime/{stockCode}")
    public ResponseEntity<String> subscribe(@PathVariable String stockCode) {
        kisWebSocketClient.subscribe(stockCode);
        kisWebSocketClient.subscribeOrderBook(stockCode);
        return ResponseEntity.ok(stockCode + " 실시간 구독 완료");
    }

    /** 실시간 구독 해제 */
    @DeleteMapping("/realtime/{stockCode}")
    public ResponseEntity<String> unsubscribe(@PathVariable String stockCode) {
        kisWebSocketClient.unsubscribe(stockCode);
        kisWebSocketClient.unsubscribeOrderBook(stockCode);
        return ResponseEntity.ok(stockCode + " 구독 해제 완료");
    }
}
