package io.antcamp.tradeservice.application.service;

import io.antcamp.tradeservice.application.service.impl.TradeServiceImpl;
import io.antcamp.tradeservice.infrastructure.client.KisApproveClient;
import io.antcamp.tradeservice.infrastructure.dto.AccessTokenRequest;
import io.antcamp.tradeservice.presentation.dto.KisAccessToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * TradeServiceImpl — KIS 액세스 토큰 발급 + Redis 캐싱 단위 테스트
 *
 * 테스트 전략
 * ─ KisApproveClient(Feign)와 StringRedisTemplate 을 @Mock 으로 대체해
 *   HTTP/Redis 호출 없이 서비스 로직만 검증한다.
 * ─ @Value 주입 필드는 ReflectionTestUtils.setField 로 주입한다.
 *
 * 서비스 로직 흐름
 *  requestAccessToken()
 *    ① Redis 조회 → 캐시 HIT  → 저장된 토큰 반환 (KIS API 미호출)
 *    ② Redis 조회 → 캐시 MISS → KIS API 호출 → Redis 저장 → 반환
 *    ③ KIS API 예외 → 빈 토큰 반환 (예외 전파 없음)
 */
@ExtendWith(MockitoExtension.class)
class TradeServiceImplTest {

    @Mock
    private KisApproveClient kisApproveClient;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TradeServiceImpl tradeService;

    private static final String DUMMY_APP_KEY    = "PStest-app-key-12345";
    private static final String DUMMY_SECRET_KEY = "PStest-secret-key-abcde";
    private static final String DUMMY_TOKEN      = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.test";

    @BeforeEach
    void setUp() {
        // @Value 주입 필드 세팅
        ReflectionTestUtils.setField(tradeService, "appKey",    DUMMY_APP_KEY);
        ReflectionTestUtils.setField(tradeService, "secretKey", DUMMY_SECRET_KEY);
        ReflectionTestUtils.setField(tradeService, "timeout",   86400);

        // StringRedisTemplate.opsForValue() 기본 스텁
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Redis 캐시 HIT
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Redis 캐시 HIT")
    class CacheHit {

        @Test
        @DisplayName("캐시에 토큰이 있으면 KIS API 를 호출하지 않고 캐시 값을 반환한다")
        void 캐시_히트시_KIS_API_미호출() {
            // given — Redis 에 토큰 존재
            given(valueOperations.get(anyString())).willReturn(DUMMY_TOKEN);

            // when
            KisAccessToken result = tradeService.requestAccessToken();

            // then
            assertThat(result.token()).isEqualTo(DUMMY_TOKEN);
            then(kisApproveClient).shouldHaveNoInteractions(); // KIS API 미호출
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Redis 캐시 MISS → KIS API 호출 성공
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("캐시 MISS — KIS API 호출 성공")
    class CacheMissSuccess {

        @BeforeEach
        void cacheMiss() {
            // Redis 에 토큰 없음 → KIS API 호출로 이어짐
            given(valueOperations.get(anyString())).willReturn(null);
        }

        @Test
        @DisplayName("KIS API 가 정상 응답하면 토큰을 반환한다")
        void 토큰이_정상적으로_반환된다() {
            // given
            given(kisApproveClient.requestAccessToken(any(AccessTokenRequest.class)))
                .willReturn(new KisAccessToken(DUMMY_TOKEN));

            // when
            KisAccessToken result = tradeService.requestAccessToken();

            // then
            assertThat(result).isNotNull();
            assertThat(result.token()).isNotBlank().isEqualTo(DUMMY_TOKEN);
        }

        @Test
        @DisplayName("토큰 발급 후 Redis 에 저장한다")
        void 발급된_토큰을_Redis에_저장한다() {
            // given
            given(kisApproveClient.requestAccessToken(any(AccessTokenRequest.class)))
                .willReturn(new KisAccessToken(DUMMY_TOKEN));

            // when
            tradeService.requestAccessToken();

            // then — Redis set 호출 여부 검증
            verify(valueOperations).set(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("KIS API 호출 시 appKey·secretKey·grantType 이 올바르게 전달된다")
        void 요청_파라미터가_올바르게_구성된다() {
            // given
            given(kisApproveClient.requestAccessToken(any(AccessTokenRequest.class)))
                .willReturn(new KisAccessToken(DUMMY_TOKEN));

            // when
            tradeService.requestAccessToken();

            // then — 실제 전달된 AccessTokenRequest 캡처해 필드별 검증
            ArgumentCaptor<AccessTokenRequest> captor =
                ArgumentCaptor.forClass(AccessTokenRequest.class);
            verify(kisApproveClient).requestAccessToken(captor.capture());

            AccessTokenRequest captured = captor.getValue();
            assertThat(captured.appKey()).isEqualTo(DUMMY_APP_KEY);
            assertThat(captured.secretKey()).isEqualTo(DUMMY_SECRET_KEY);
            assertThat(captured.grantType()).isEqualTo("manager");
        }

        @Test
        @DisplayName("KIS API 는 정확히 한 번만 호출된다")
        void KIS_API_는_정확히_한_번_호출된다() {
            // given
            given(kisApproveClient.requestAccessToken(any(AccessTokenRequest.class)))
                .willReturn(new KisAccessToken(DUMMY_TOKEN));

            // when
            tradeService.requestAccessToken();

            // then
            then(kisApproveClient)
                .should()
                .requestAccessToken(any(AccessTokenRequest.class));
            then(kisApproveClient).shouldHaveNoMoreInteractions();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Redis 캐시 MISS → KIS API 호출 실패
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("캐시 MISS — KIS API 호출 실패")
    class CacheMissFailure {

        @BeforeEach
        void cacheMiss() {
            given(valueOperations.get(anyString())).willReturn(null);
        }

        @Test
        @DisplayName("KIS API 예외 시 빈 토큰을 반환하고 예외를 전파하지 않는다")
        void KIS_API_실패시_빈_토큰을_반환한다() {
            // given
            given(kisApproveClient.requestAccessToken(any(AccessTokenRequest.class)))
                .willThrow(new RuntimeException("KIS API 서버 응답 없음"));

            // when
            KisAccessToken result = tradeService.requestAccessToken();

            // then
            assertThat(result.token()).isEmpty();
            // 예외 시 Redis 저장 미호출
            verify(valueOperations, never()).set(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("타임아웃 예외도 graceful 하게 처리한다")
        void 타임아웃시_빈_토큰을_반환한다() {
            // given — Feign 은 SocketTimeoutException 을 RuntimeException 으로 래핑
            given(kisApproveClient.requestAccessToken(any(AccessTokenRequest.class)))
                .willThrow(new RuntimeException("Connection timed out after 5000ms"));

            // when
            KisAccessToken result = tradeService.requestAccessToken();

            // then
            assertThat(result.token()).isEmpty();
        }

        @Test
        @DisplayName("401 FeignException 도 graceful 하게 처리한다")
        void 인증_실패시_빈_토큰을_반환한다() {
            // given
            byte[] emptyBody = new byte[0];
            feign.Request dummyRequest = feign.Request.create(
                feign.Request.HttpMethod.POST,
                "https://openapi.koreainvestment.com:9443/oauth2/Approval",
                java.util.Collections.emptyMap(),
                emptyBody,
                java.nio.charset.StandardCharsets.UTF_8,
                null
            );
            given(kisApproveClient.requestAccessToken(any(AccessTokenRequest.class)))
                .willThrow(new feign.FeignException.Unauthorized(
                    "401 Unauthorized — appkey/secretkey 불일치",
                    dummyRequest, emptyBody, null
                ));

            // when
            KisAccessToken result = tradeService.requestAccessToken();

            // then
            assertThat(result.token()).isEmpty();
        }
    }
}
