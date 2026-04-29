package io.antcamp.tradeservice.application.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.antcamp.tradeservice.application.service.TradeService;
import io.antcamp.tradeservice.infrastructure.client.KisClient;
import io.antcamp.tradeservice.infrastructure.dto.AccessTokenRequest;
import io.antcamp.tradeservice.infrastructure.dto.AccessTokenResponse;
import io.antcamp.tradeservice.infrastructure.exception.KisApiException;
import io.antcamp.tradeservice.presentation.dto.MinutePriceRequestHeader;
import io.antcamp.tradeservice.presentation.dto.MinutePriceRequestParam;
import io.antcamp.tradeservice.presentation.dto.MinutePriceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class TradeServiceImpl implements TradeService {

    private final StringRedisTemplate redisTemplate;
    private final KisClient kisClient;
    private final ObjectMapper objectMapper;

    @Value("${kis.app.key}")
    private String appKey;
    @Value("${kis.app.secret}")
    private String secretKey;
    @Value("${spring.data.redis.timeout:86400}")
    private int timeout;

    private static final String ACCESS_TOKEN_KEY = "kis:access-token";
    private static final String GRANT_TYPE = "client_credentials";

    // ── 토큰 발급 ──────────────────────────────────────────────────────────

    @Override
    public AccessTokenResponse requestAccessToken() {
        String cachedToken = getAccessToken();
        if (cachedToken != null && !cachedToken.isEmpty()) {
            return new AccessTokenResponse(cachedToken,null, null,null );
        }

        AccessTokenRequest request = new AccessTokenRequest(GRANT_TYPE, appKey, secretKey);
        AccessTokenResponse token = kisClient.requestAccessToken(request);  // 실패 시 ErrorDecoder 가 처리

        if (token.accessToken() != null && !token.accessToken().isEmpty()) {
            saveAccessToken(token.accessToken(), timeout);
        }
        return token;
    }

    @Override
    public String requestApprovalKey() {
        AccessTokenRequest request = new AccessTokenRequest(GRANT_TYPE, appKey, secretKey);
        return kisClient.requestApprovalKey(request).approvalKey();
    }

    // ── 분봉 조회 ──────────────────────────────────────────────────────────

    @Override
    public MinutePriceResponse getMinutePrice(String stockCode, LocalDateTime dateTime)  {
        String date = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String time = dateTime.format(DateTimeFormatter.ofPattern("HHmmss"));

        Map<String ,Object> header = MinutePriceRequestHeader.create(
                "Bearer " + requestAccessToken().accessToken(),
                appKey,
                secretKey
        );
        Map<String ,Object> param = MinutePriceRequestParam.create(stockCode, time, date);

        String response = kisClient.getMinutePrice(header, param);
        System.out.println(response);
        try{
            MinutePriceResponse result = objectMapper.readValue(response, MinutePriceResponse.class);
            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /** 3회 재시도 후 최종 실패 시 폴백 */
    private MinutePriceResponse getMinutePriceFallback(String stockCode,
                                                        LocalDateTime dateTime,
                                                        Exception e) {
        if (e instanceof KisApiException ex) {
            log.error("KIS 분봉 조회 실패 [{}] stockCode={}: {}", ex.getErrorCode(), stockCode, ex.getMessage());
        } else {
            log.error("KIS 분봉 조회 최종 실패 stockCode={}: {}", stockCode, e.getMessage());
        }
        return MinutePriceResponse.empty();  // 빈 응답 반환 (서킷 오픈 대신 graceful degradation)
    }

    // ── 공통 ───────────────────────────────────────────────────────────────

    @Override
    public void clearAll() {
        redisTemplate.delete(ACCESS_TOKEN_KEY);
    }

    private void saveAccessToken(String token, long expiresInSeconds) {
        redisTemplate.opsForValue().set(ACCESS_TOKEN_KEY, token, Duration.ofHours(23));
    }

    private String getAccessToken() {
        return redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
    }
}
