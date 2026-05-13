package io.antcamp.tradeservice.infrastructure.client;

import io.antcamp.tradeservice.infrastructure.config.OpenFeignConfig;
import io.antcamp.tradeservice.infrastructure.dto.AccessTokenRequest;
import io.antcamp.tradeservice.presentation.dto.KisAccessToken;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// configuration = OpenFeignConfig.class → KIS 전용 ErrorDecoder 적용
@FeignClient(name = "kisApproveClient", url = "${kis.app.url}", configuration = OpenFeignConfig.class)
public interface KisApproveClient {

    @PostMapping("/oauth2/Approval")
    KisAccessToken requestAccessToken(@RequestBody AccessTokenRequest request);
}
