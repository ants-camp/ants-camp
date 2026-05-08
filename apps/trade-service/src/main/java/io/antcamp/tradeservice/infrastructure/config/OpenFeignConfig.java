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
                String rawBody = new String(body, java.nio.charset.StandardCharsets.UTF_8);

                // 404 등 비정상 HTTP 상태는 URL + body를 함께 출력
                if (response.status() == 404) {
                    log.error("KIS API 404 — url={} body={}",
                            response.request().url(), rawBody);
                    return new KisApiException("HTTP_404",
                            "KIS API 경로 없음 [url=" + response.request().url() + "]");
                }

                KisErrorResponse error = objectMapper.readValue(body, KisErrorResponse.class);
                log.error("KIS API 오류 [{}] status={} msg_cd={} msg={}",
                        methodKey, response.status(), error.msgCd(), error.message());

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

            } catch (KisApiException e) {
                throw e;
            } catch (Exception e) {
                log.error("KIS API 에러 응답 파싱 실패 [{}] status={}: {}",
                        methodKey, response.status(), e.getMessage());
                return new ErrorDecoder.Default().decode(methodKey, response);
            }
        };
    }
}
