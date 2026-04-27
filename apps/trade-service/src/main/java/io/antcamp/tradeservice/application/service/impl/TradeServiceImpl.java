package io.antcamp.tradeservice.application.service.impl;

import io.antcamp.tradeservice.application.service.TradeService;
import io.antcamp.tradeservice.infrastructure.client.KisApproveClient;
import io.antcamp.tradeservice.infrastructure.dto.AccessTokenRequest;
import io.antcamp.tradeservice.presentation.dto.KisAccessToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Service
public class TradeServiceImpl implements TradeService {

    private final StringRedisTemplate redisTemplate;
    private final KisApproveClient kisApproveClient;

    @Value("${kis.app.key}")
    private String appKey;
    @Value("${kis.app.secret}")
    private String secretKey;
    @Value("${spring.data.redis.timeout:86400}")
    private int timeout;

    private static final String ACCESS_TOKEN_KEY = "kis:access-token";
    private static final String GRANT_TYPE = "manager";

    @Override
    public KisAccessToken requestAccessToken() {

        // 1. redis 조회
        String cachedToken = getAccessToken();
        if(cachedToken != null){
            return new KisAccessToken(cachedToken);
        }

        // 2. 없으면 발급
        String newToken = "";
        AccessTokenRequest request = new AccessTokenRequest(
                GRANT_TYPE, appKey, secretKey
        );
        try {
            KisAccessToken token = kisApproveClient.requestAccessToken(request);
            log.info("@@token = {}", token.token());
            // 3. redis 저장
            saveAccessToken(token.token(), timeout);
            return token;
        } catch (Exception e) {
            return new KisAccessToken("");
        }
    }

    private void saveAccessToken(String accessToken, long expiresInSeconds) {
        redisTemplate.opsForValue().set(
                ACCESS_TOKEN_KEY,
                accessToken,
                Duration.ofHours(23)
        );
    }

    private String getAccessToken() {
        return redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
    }

    @Override
    public void clearAll() {
        redisTemplate.delete(ACCESS_TOKEN_KEY);
    }
}
