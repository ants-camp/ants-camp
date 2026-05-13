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

}
