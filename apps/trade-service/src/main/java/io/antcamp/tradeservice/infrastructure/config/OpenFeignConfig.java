package io.antcamp.tradeservice.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import io.antcamp.tradeservice.infrastructure.dto.KisErrorResponse;
import io.antcamp.tradeservice.infrastructure.exception.KisApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OpenFeignConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public ErrorDecoder kisErrorDecoder() {
        return (methodKey, response) -> {
            // body 가 없는 경우
            if (response.body() == null) {
                return new KisApiException("UNKNOWN", "KIS API 응답 body 없음 [status=" + response.status() + "]");
            }

            try {
                byte[] body = response.body().asInputStream().readAllBytes();
                KisErrorResponse error = objectMapper.readValue(body, KisErrorResponse.class);

                log.error("KIS API 오류 [{}] [{}]: {}", methodKey, error.msgCd(), error.message());

                // 재시도 가능한 에러 (토큰 만료, 요청 초과) → RetryableException 으로 감싸기
                // → Feign Retryer 또는 Resilience4j @Retry 가 자동 재시도
                if (error.isRetryable()) {
                    return new RetryableException(
                            response.status(),
                            error.message(),
                            response.request().httpMethod(),
                            (Long) null,
                            response.request()
                    );
                }

                return new KisApiException(error.msgCd(), error.message());

            } catch (Exception e) {
                log.error("KIS API 에러 응답 파싱 실패 [{}]: {}", methodKey, e.getMessage());
                // 파싱 실패 시 기본 FeignException 으로 폴백
                return new ErrorDecoder.Default().decode(methodKey, response);
            }
        };
    }
}
