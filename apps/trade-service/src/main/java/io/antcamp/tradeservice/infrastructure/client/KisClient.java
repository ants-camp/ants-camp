package io.antcamp.tradeservice.infrastructure.client;

import io.antcamp.tradeservice.infrastructure.dto.AccessTokenRequest;
import io.antcamp.tradeservice.infrastructure.dto.AccessTokenResponse;
import io.antcamp.tradeservice.infrastructure.dto.ApprovalKeyResponse;
import io.antcamp.tradeservice.presentation.dto.KisAccessToken;
import io.antcamp.tradeservice.presentation.dto.MinutePriceRequestHeader;
import io.antcamp.tradeservice.presentation.dto.MinutePriceRequestParam;
import io.antcamp.tradeservice.presentation.dto.MinutePriceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "kisClient", url = "${kis.app.url}")
public interface KisClient {

    // REST API 접근 토큰 발급 — 응답: {"access_token": "..."}
    @PostMapping("/oauth2/tokenP")
    AccessTokenResponse requestAccessToken(@RequestBody AccessTokenRequest request);

    // WebSocket 접속키 발급 — 응답: {"approval_key": "..."}
    @PostMapping("/oauth2/Approval")
    ApprovalKeyResponse requestApprovalKey(@RequestBody AccessTokenRequest request);

    @GetMapping("/uapi/domestic-stock/v1/quotations/inquire-time-dailychartprice")
    String getMinutePrice(
            @RequestHeader Map<String ,Object> header,
            @RequestParam Map<String ,Object> param
    );
}
