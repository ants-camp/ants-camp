package io.antcamp.tradeservice.infrastructure.client;

import io.antcamp.tradeservice.presentation.dto.AssetResponse;
import io.antcamp.tradeservice.presentation.dto.AssetSellRequest;
import io.antcamp.tradeservice.presentation.dto.BuyHoldingRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "assetClient", url = "${feign.url.asset}")
public interface AssetClient {

    // 수정 전:
    // @PostMapping("/api/holdings/buy")
    // AssetResponse getAsset(@RequestParam UUID accountId,
    //                        @RequestBody double price);
    @PostMapping("/api/holdings/buy")
    AssetResponse getAsset(@RequestHeader("X-User-Id") UUID userId,
                           @RequestBody BuyHoldingRequest command);

    // 수정 전:
    // @PostMapping("/api/holdings/sell")
    // AssetResponse getStock(@RequestBody AssetSellRequest request);
    @PostMapping("/api/holdings/sell")
    AssetResponse getStock(@RequestHeader("X-User-Id") UUID userId,
                           @RequestBody AssetSellRequest request);

    // ── 내부 검증용 (X-User-Id 불필요) ───────────────────────────────────────
    // 지정가 PENDING 접수 시점에 잔액/보유 사전 검증을 위해 추가.

    /** 특정 계좌·종목의 보유 수량. 보유 없으면 0. */
    @GetMapping("/internal/holdings/quantity")
    Integer getHoldingQuantity(@RequestParam("accountId") UUID accountId,
                               @RequestParam("stockCode") String stockCode);

    /** 특정 계좌의 현금 잔액. */
    @GetMapping("/internal/accounts/balance")
    Long getAccountBalance(@RequestParam("accountId") UUID accountId);
}
