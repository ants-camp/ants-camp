package io.antcamp.tradeservice.infrastructure.client;

import io.antcamp.tradeservice.infrastructure.dto.AccessTokenRequest;
import io.antcamp.tradeservice.infrastructure.dto.AccessTokenResponse;
import io.antcamp.tradeservice.infrastructure.dto.ApprovalKeyResponse;
import io.antcamp.tradeservice.presentation.dto.AssetResponse;
import io.antcamp.tradeservice.presentation.dto.BuyStockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "assetClient", url = "${feign.url.asset}")
public interface AssetClient {

    @PostMapping("/account")
    AssetResponse getAsset(@RequestParam UUID accountId,
                           @RequestBody double price);

}
