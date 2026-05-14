package io.antcamp.tradeservice.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import io.antcamp.tradeservice.infrastructure.dto.KisErrorResponse;
import io.antcamp.tradeservice.infrastructure.exception.KisApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

/**
 * KIS Feign 클라이언트 전용 설정.
 *
 * <p>주의: 이 클래스에는 {@code @Configuration} 어노테이션을 절대 붙이지 말 것.
 * 컴포넌트 스캔에 잡히면 ErrorDecoder가 모든 Feign 클라이언트(AssetClient 등)에 글로벌로 적용되어,
 * KIS가 아닌 서비스의 에러 응답을 KIS 포맷({@code msg_cd}, {@code msg})으로 강제 파싱하여
 * 실제 에러 메시지를 null로 만들어버린다.
 *
 * <p>이 설정은 {@code @FeignClient(configuration = OpenFeignConfig.class)}로
 * 명시적으로 지정한 클라이언트({@link io.antcamp.tradeservice.infrastructure.client.KisClient},
 * {@link io.antcamp.tradeservice.infrastructure.client.KisApproveClient})에만 적용된다.
 */
@Slf4j
public class OpenFeignConfig {

    @Bean
    public ErrorDecoder kisErrorDecoder(ObjectMapper objectMapper) {
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
