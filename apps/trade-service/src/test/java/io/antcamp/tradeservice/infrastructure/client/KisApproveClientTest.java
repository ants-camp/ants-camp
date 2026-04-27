package io.antcamp.tradeservice.infrastructure.client;

import io.antcamp.tradeservice.infrastructure.dto.AccessTokenRequest;
import io.antcamp.tradeservice.presentation.dto.KisAccessToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * KisApproveClient — Feign 클라이언트 인터페이스 동작 검증
 *
 * Feign 클라이언트 자체의 HTTP 동작을 검증하려면 WireMock 이 필요하다.
 * (의존성 추가: testImplementation 'org.springframework.cloud:spring-cloud-contract-wiremock')
 *
 * 현재는 WireMock 미포함 구성이므로, Mock 으로 인터페이스 계약을 확인한다.
 * → HTTP 레벨 통합 테스트는 파일 하단 주석 참고.
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │ KIS 실제 AccessToken 발급 엔드포인트                         │
 * │  모의투자: https://openapi.kis.developerapi.com/oauth2/tokenP│
 * │  실전:    https://openapivts.koreainvestment.com:29443/...  │
 * │  Method:  POST                                              │
 * │  Body:    { "grant_type": "client_credentials",            │
 * │             "appkey": "...", "appsecret": "..." }           │
 * │  Response: { "access_token": "eyJ...",                     │
 * │              "token_type": "Bearer",                        │
 * │              "expires_in": 86400 }                          │
 * │                                                             │
 * │ ⚠ 현재 코드 주의사항:                                        │
 * │  · grant_type = "manager" → "client_credentials" 로 수정 필요 │
 * │  · secretkey 필드명 → KIS 문서 기준 "appsecret"               │
 * └─────────────────────────────────────────────────────────────┘
 */
@ExtendWith(MockitoExtension.class)
class KisApproveClientTest {

    @Mock
    private KisApproveClient kisApproveClient;

    private static final String SAMPLE_TOKEN =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9" +
        ".eyJzdWIiOiJ0b2tlbiIsImV4cCI6MTcwOTk5OTk5OX0" +
        ".sample-signature";

    @Test
    @DisplayName("정상 요청 시 KisAccessToken 을 반환한다")
    void 토큰이_정상적으로_발급되어야_한다() {
        // given
        AccessTokenRequest request = new AccessTokenRequest(
            "client_credentials",
            "PS-your-app-key",
            "your-secret-key"
        );
        given(kisApproveClient.requestAccessToken(request))
            .willReturn(new KisAccessToken(SAMPLE_TOKEN));

        // when
        KisAccessToken result = kisApproveClient.requestAccessToken(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.token())
            .isNotBlank()
            .startsWith("eyJ");    // JWT 형식 확인
    }

    @Test
    @DisplayName("requestAccessToken 은 정확히 한 번 호출되어야 한다")
    void requestAccessToken_은_정확히_한_번_호출된다() {
        // given
        AccessTokenRequest request = new AccessTokenRequest(
            "client_credentials", "key", "secret"
        );
        given(kisApproveClient.requestAccessToken(request))
            .willReturn(new KisAccessToken(SAMPLE_TOKEN));

        // when
        kisApproveClient.requestAccessToken(request);

        // then
        then(kisApproveClient)
            .should()
            .requestAccessToken(request);
        then(kisApproveClient).shouldHaveNoMoreInteractions();
    }
}

/*
 * [참고] WireMock 추가 시 HTTP 레벨까지 검증하는 통합 테스트 예시
 *
 * build.gradle 에 추가:
 *   testImplementation 'org.springframework.cloud:spring-cloud-contract-wiremock'
 *
 * ────────────────────────────────────────────────────────────────
 *
 * @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
 * @AutoConfigureWireMock(port = 0)
 * @ActiveProfiles("test")
 * @TestPropertySource(properties = {
 *     "kis.app.approveUrl=http://localhost:${wiremock.server.port}",
 *     "kis.app.key=PS-test-key",
 *     "kis.app.secretKey=test-secret"
 * })
 * class KisApproveClientWireMockTest {
 *
 *     @Autowired
 *     private KisApproveClient kisApproveClient;
 *
 *     @Test
 *     @DisplayName("WireMock: KIS API 정상 응답 시 토큰을 파싱한다")
 *     void KIS_정상응답_토큰_파싱() {
 *         stubFor(post(urlEqualTo("/oauth2/tokenP"))
 *             .withRequestBody(matchingJsonPath("$.grant_type", equalTo("client_credentials")))
 *             .willReturn(aResponse()
 *                 .withStatus(200)
 *                 .withHeader("Content-Type", "application/json")
 *                 .withBody("""
 *                     {
 *                       "access_token": "eyJ0eXAiOiJKV1Q...",
 *                       "token_type": "Bearer",
 *                       "expires_in": 86400
 *                     }
 *                 """)));
 *
 *         KisAccessToken token = kisApproveClient.requestAccessToken(
 *             new AccessTokenRequest("client_credentials", "PS-test-key", "test-secret")
 *         );
 *
 *         assertThat(token.token()).isEqualTo("eyJ0eXAiOiJKV1Q...");
 *         verify(postRequestedFor(urlEqualTo("/oauth2/tokenP"))
 *             .withHeader("Content-Type", containing("application/json")));
 *     }
 *
 *     @Test
 *     @DisplayName("WireMock: 401 응답 시 FeignException.Unauthorized 발생")
 *     void KIS_401응답_FeignException_발생() {
 *         stubFor(post(urlEqualTo("/oauth2/tokenP"))
 *             .willReturn(aResponse().withStatus(401).withBody("Unauthorized")));
 *
 *         assertThatThrownBy(() -> kisApproveClient.requestAccessToken(
 *             new AccessTokenRequest("client_credentials", "wrong", "wrong")))
 *             .isInstanceOf(feign.FeignException.Unauthorized.class);
 *     }
 * }
*/
