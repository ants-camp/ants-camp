package io.antcamp.assetservice.presentation.controller;

import io.antcamp.assetservice.application.service.AccountService;
import io.antcamp.assetservice.application.service.HoldingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 서비스 간 호출 전용 엔드포인트.
 *
 * <p>X-User-Id 헤더를 요구하지 않고, 권한 검증을 생략한다.
 * 게이트웨이에서 외부 노출되지 않도록 라우팅 단계에서 차단할 것 (경로 prefix: {@code /internal/...}).
 *
 * <p>현재 trade-service 가 지정가 PENDING 접수 시점에 잔액/보유 사전 검증을 위해 호출.
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalAssetController {

    private final HoldingService holdingService;
    private final AccountService accountService;

    /**
     * 보유 수량 조회. 보유가 없으면 0.
     */
    @GetMapping("/holdings/quantity")
    public ResponseEntity<Integer> getHoldingQuantity(
            @RequestParam UUID accountId,
            @RequestParam String stockCode
    ) {
        return ResponseEntity.ok(holdingService.getHoldingQuantity(accountId, stockCode));
    }

    /**
     * 현금 잔액 조회.
     */
    @GetMapping("/accounts/balance")
    public ResponseEntity<Long> getAccountBalance(
            @RequestParam UUID accountId
    ) {
        return ResponseEntity.ok(accountService.getBalance(accountId));
    }
}
