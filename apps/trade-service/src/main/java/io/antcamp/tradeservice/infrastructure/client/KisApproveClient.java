package io.antcamp.tradeservice.infrastructure.client;

import io.antcamp.tradeservice.infrastructure.dto.AccessTokenRequest;
import io.antcamp.tradeservice.presentation.dto.KisAccessToken;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "kisApproveClient", url = "${kis.app.url}")
public interface KisApproveClient {

    @PostMapping("/oauth2/Approval")
    KisAccessToken requestAccessToken(@RequestBody AccessTokenRequest request);
}
