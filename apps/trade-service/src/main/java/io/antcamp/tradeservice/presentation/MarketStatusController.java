package io.antcamp.tradeservice.presentation;

import io.antcamp.tradeservice.application.service.MarketStatusService;
import io.antcamp.tradeservice.infrastructure.dto.MarketStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 장 운영 상태 REST API
 *
 * GET /api/market/status
 *   → { status, openTime, closeTime, isHoliday, message }
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/market")
public class MarketStatusController {

    private final MarketStatusService marketStatusService;

    @GetMapping("/status")
    public ResponseEntity<MarketStatusResponse> getStatus() {
        return ResponseEntity.ok(marketStatusService.getStatus());
    }
}
