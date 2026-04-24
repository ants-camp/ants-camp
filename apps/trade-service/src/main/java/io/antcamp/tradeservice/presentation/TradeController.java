package io.antcamp.tradeservice.presentation;

import common.dto.ApiResponse;
import io.antcamp.tradeservice.application.service.TradeService;
import io.antcamp.tradeservice.presentation.dto.KisAccessToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trades")
public class TradeController {
    private final TradeService tradeService;

    public ResponseEntity<ApiResponse<KisAccessToken>> getKisAccessToken(){
        return ApiResponse.ok(tradeService.getAccessToken());
    }
}
