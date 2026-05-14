# Swagger 구현 계획

## 전략 요약

- **패턴**: `*ControllerDocs` 인터페이스에 Swagger 어노테이션 전부 선언 → 기존 Controller가 `implements`
- **라이브러리**: `springdoc-openapi-starter-webmvc-ui:2.5.0` (Spring Boot 3.x 호환)
- **적용 서비스**: user-service, asset-service, trade-service, competition-service, ranking-service, assistant-service, notification-service
- **제외 서비스**: api-gateway, eureka-server, config-server (인프라 서버)

---

## 1. 공통 설정

### 1-1. 의존성 추가 (서비스별 `build.gradle`)

각 서비스의 `build.gradle`에 아래를 추가합니다.

```gradle
// 7개 서비스 각각의 build.gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0'
```

### 1-2. SwaggerConfig (서비스별 동일 패턴, 서비스명만 다름)

```
{service}/src/main/java/io/antcamp/{service}/config/SwaggerConfig.java
```

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("User Service API")        // 서비스명 변경
                .description("사용자 인증/관리 API") // 서비스 설명 변경
                .version("v1.0.0"))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
            .components(new Components()
                .addSecuritySchemes("Bearer Auth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

### 1-3. 인터페이스 패키지 구조

기존 `controller` 패키지 내에 `docs` 서브패키지를 만들고 인터페이스를 위치시킵니다.

```
presentation/
  controller/
    docs/
      AuthControllerDocs.java      ← Swagger 어노테이션 전담
    AuthController.java            ← implements AuthControllerDocs, 비즈니스 로직만
```

### 1-4. 컨트롤러 리팩터링 패턴

**Before (기존):**
```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) { ... }
}
```

**After (변경 후):**
```java
// ── Docs 인터페이스 ──────────────────────────────────────
@Tag(name = "Auth", description = "인증 API")
public interface AuthControllerDocs {

    @Operation(summary = "로그인")
    @ApiResponses({ ... })
    @PostMapping("/login")
    ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request);
}

// ── 컨트롤러 ─────────────────────────────────────────────
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    @Override
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.ok("로그인에 성공했습니다.", response);
    }
}
```

> **핵심 규칙**
> - HTTP 매핑(`@PostMapping` 등), `@RequestBody`, `@PathVariable`, `@RequestParam`, `@RequestHeader` → 인터페이스에 선언
> - `@Valid` → 컨트롤러 구현부에 유지 (Bean Validation은 구현체에서 동작)
> - `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor` → 컨트롤러에만 유지
> - Swagger 어노테이션 (`@Tag`, `@Operation`, `@ApiResponses`, `@Parameter`) → 인터페이스에만 선언

---

## 2. 서비스별 구현 명세

### 응답 Example JSON 공통 포맷

**성공 (ApiResponse 기반)**
```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "요청에 성공했습니다.",
  "data": { ... }
}
```

**실패 (ApiResponse 기반)**
```json
{
  "status": 401,
  "code": "LOGIN_FAILED",
  "message": "아이디 또는 비밀번호가 올바르지 않습니다.",
  "data": null
}
```

---

### 2-1. user-service

#### AuthControllerDocs

| 메서드 | 엔드포인트 | 성공 예시 코드 | 실패 예시 (ErrorCode) |
|---|---|---|---|
| POST | `/api/auth/login` | 200 LoginResponse | 401 `LOGIN_FAILED` |
| POST | `/api/auth/logout` | 200 null | 401 `INVALID_TOKEN` |
| POST | `/api/auth/reissue` | 200 LoginResponse | 401 `INVALID_REFRESH_TOKEN` |

```java
@Tag(name = "Auth", description = "로그인 / 로그아웃 / 토큰 재발급")
public interface AuthControllerDocs {

    @Operation(summary = "로그인", description = "이메일·비밀번호로 로그인하고 JWT를 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "status": 200, "code": "SUCCESS", "message": "로그인에 성공했습니다.",
                      "data": {
                        "accessToken": "eyJhbGci...",
                        "refreshToken": "eyJhbGci...",
                        "user": {
                          "userId": "550e8400-e29b-41d4-a716-446655440000",
                          "email": "user@example.com",
                          "name": "홍길동",
                          "role": "PLAYER",
                          "phone": "010-1234-5678"
                        }
                      }
                    }"""))),
        @ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "status": 401, "code": "LOGIN_FAILED",
                      "message": "아이디 또는 비밀번호가 올바르지 않습니다.", "data": null
                    }""")))
    })
    @PostMapping("/login")
    ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request);

    @Operation(summary = "로그아웃", description = "액세스 토큰을 블랙리스트에 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그아웃 성공",
            content = @Content(examples = @ExampleObject(value = """
                {"status":200,"code":"SUCCESS","message":"로그아웃에 성공했습니다.","data":null}"""))),
        @ApiResponse(responseCode = "401", description = "이미 로그아웃된 토큰",
            content = @Content(examples = @ExampleObject(value = """
                {"status":401,"code":"TOKEN_BLACKLISTED","message":"이미 로그아웃된 토큰입니다.","data":null}""")))
    })
    @PostMapping("/logout")
    ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest request);

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새 Access Token을 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "재발급 성공",
            content = @Content(examples = @ExampleObject(value = """
                {"status":200,"code":"SUCCESS","message":"토큰 재발급에 성공했습니다.",
                 "data":{"accessToken":"eyJhbGci...","refreshToken":"eyJhbGci...","user":{...}}}"""))),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token",
            content = @Content(examples = @ExampleObject(value = """
                {"status":401,"code":"INVALID_REFRESH_TOKEN","message":"유효하지 않은 리프레시 토큰입니다.","data":null}""")))
    })
    @PostMapping("/reissue")
    ResponseEntity<ApiResponse<LoginResponse>> reissue(@RequestBody ReissueRequest request);
}
```

#### UserControllerDocs

| 메서드 | 엔드포인트 | 성공 | 실패 |
|---|---|---|---|
| POST | `/api/users/register` | 201 UserResponse | 409 `DUPLICATE_EMAIL` |
| GET | `/api/users/me` | 200 UserResponse | 404 `USER_NOT_FOUND` |

#### AdminUserControllerDocs

| 메서드 | 엔드포인트 | 성공 | 실패 |
|---|---|---|---|
| POST | `/api/admin/users/manager` | 201 UserResponse | 409 `DUPLICATE_EMAIL` |
| GET | `/api/admin/users` | 200 `List<UserResponse>` | 403 `FORBIDDEN` |

---

### 2-2. asset-service

#### AccountControllerDocs

| 메서드 | 엔드포인트 | 성공 | 실패 |
|---|---|---|---|
| POST | `/api/accounts` | 201 AccountResponse | 400 `INVALID_INPUT` |
| POST | `/api/accounts/{accountId}/deposit` | 200 BalanceResponse | 400 `INVALID_INPUT_VALUE` |
| POST | `/api/accounts/{accountId}/withdraw` | 200 BalanceResponse | 409 `INSUFFICIENT_BALANCE` |
| GET | `/api/accounts/{accountId}` | 200 AccountResult | 404 `ACCOUNT_NOT_FOUND` |
| GET | `/api/accounts` | 200 `List<AccountResult>` | 403 `UNAUTHORIZED_ACCESS` |

```java
@Tag(name = "Account", description = "계좌 생성 / 입출금 / 조회")
public interface AccountControllerDocs {

    @Operation(summary = "계좌 생성")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "계좌 생성 성공",
            content = @Content(examples = @ExampleObject(value = """
                {"status":201,"code":"SUCCESS","message":"계좌 생성에 성공했습니다.",
                 "data":{"accountId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890"}}"""))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (userId 없음 등)",
            content = @Content(examples = @ExampleObject(value = """
                {"status":400,"code":"INVALID_INPUT","message":"입력값이 유효하지 않습니다.","data":null}""")))
    })
    @PostMapping
    ResponseEntity<ApiResponse<AccountResponse>> createAccount(@RequestBody CreateAccountCommand command);

    @Operation(summary = "입금",
        parameters = @Parameter(name = "accountId", description = "계좌 UUID", required = true))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "입금 성공",
            content = @Content(examples = @ExampleObject(value = """
                {"status":200,"code":"SUCCESS","message":"입금에 성공했습니다.",
                 "data":{"accountId":"a1b2c3d4-...","balance":1500000}}"""))),
        @ApiResponse(responseCode = "400", description = "금액이 0 이하",
            content = @Content(examples = @ExampleObject(value = """
                {"status":400,"code":"INVALID_INPUT_VALUE","message":"금액은 0보다 커야 합니다.","data":null}""")))
    })
    @PostMapping("/{accountId}/deposit")
    ResponseEntity<ApiResponse<BalanceResponse>> deposit(
        @PathVariable UUID accountId,
        @RequestParam Long amount);

    // withdraw, getAccount, getMyAccounts 동일 패턴
    ...
}
```

#### AssetControllerDocs

| 메서드 | 엔드포인트 | 성공 | 실패 |
|---|---|---|---|
| GET | `/api/assets?accountId=` | 200 AssetResult | 403 `UNAUTHORIZED_ACCESS` |

#### HoldingControllerDocs

| 메서드 | 엔드포인트 | 성공 | 실패 |
|---|---|---|---|
| POST | `/api/holdings/buy` | 200 TradeResult | 409 `INSUFFICIENT_BALANCE` |
| POST | `/api/holdings/sell` | 200 TradeResult | 404 `HOLDING_NOT_FOUND` |
| GET | `/api/holdings?accountId=` | 200 `List<HoldingResponse>` | 403 `UNAUTHORIZED_ACCESS` |

---

### 2-3. trade-service

#### TradeControllerDocs

| 메서드 | 엔드포인트 | 성공 | 실패 |
|---|---|---|---|
| POST | `/api/trades/access-token` | 200 AccessTokenResponse | 503 `KIS_SERVER_ERROR` |
| GET | `/api/trades/minute-price` | 200 Double | 503 `KIS_SERVER_ERROR` |
| GET | `/api/trades/now-price` | 200 Double | 503 `KIS_SERVER_ERROR` |
| GET | `/api/trades/price` | 200 MinutePriceResponse | 503 `KIS_SERVER_ERROR` |
| GET | `/api/trades/chart` | 200 DailyChartResponse | 503 `KIS_SERVER_ERROR` |
| POST | `/api/trades/stock-price-list` | 200 StockPriceList | 503 `KIS_SERVER_ERROR` |
| POST | `/api/trades/order` | 200 TradeOrderResponse | 404 `TRADE_NOT_FOUND` |
| DELETE | `/api/trades/order/{tradeId}` | 200 TradeOrderResponse | 409 `TRADE_ALREADY_PROCESSED` |
| GET | `/api/trades/pending` | 200 `List<PendingOrderResponse>` | 400 `INVALID_INPUT` |

```java
@Tag(name = "Trade", description = "주식 가격 조회 / 주문 / 미체결 관리")
public interface TradeControllerDocs {

    @Operation(summary = "주문 접수",
        description = """
            시장가/지정가 매수·매도를 통합 처리합니다.
            - EXECUTED: 즉시 체결
            - PENDING: 미체결 대기 (지정가 조건 미충족)
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 접수 성공",
            content = @Content(examples = @ExampleObject(value = """
                {
                  "status": 200, "code": "SUCCESS", "message": "요청에 성공했습니다.",
                  "data": {
                    "tradeId": "550e8400-e29b-41d4-a716-446655440000",
                    "stockCode": "005930",
                    "stockAmount": 10,
                    "orderType": "MARKET",
                    "side": "BUY",
                    "status": "EXECUTED",
                    "executedPrice": 75000.0,
                    "executedAt": "2026-05-11T10:30:00"
                  }
                }"""))),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 계좌",
            content = @Content(examples = @ExampleObject(value = """
                {"status":404,"code":"TRADE_NOT_FOUND","message":"존재하지 않는 매매입니다.","data":null}""")))
    })
    @PostMapping("/order")
    ResponseEntity<ApiResponse<TradeOrderResponse>> placeOrder(
        @RequestBody TradeOrderRequest request,
        @Parameter(hidden = true) @LoginAccount UUID accountId);  // 커스텀 어노테이션은 hidden 처리

    ...
}
```

> **주의**: `@LoginAccount` 같은 커스텀 리졸버 파라미터는 `@Parameter(hidden = true)`로 Swagger UI에서 숨깁니다.

#### StockControllerDocs

| 메서드 | 엔드포인트 | 성공 | 실패 |
|---|---|---|---|
| GET | `/api/stocks/search` | 200 `List<StockSearchResponse>` | 503 `KIS_SERVER_ERROR` |
| POST | `/api/stocks/realtime/{stockCode}` | 200 String | 503 `KIS_SERVER_ERROR` |
| DELETE | `/api/stocks/realtime/{stockCode}` | 200 String | 503 `KIS_SERVER_ERROR` |

#### MarketStatusControllerDocs

| 메서드 | 엔드포인트 | 성공 | 실패 |
|---|---|---|---|
| GET | `/api/market/status` | 200 MarketStatusResponse | 503 `KIS_SERVER_ERROR` |

---

### 2-4. competition-service

#### CompetitionControllerDocs

| 메서드 | 엔드포인트 | 성공 | 실패 |
|---|---|---|---|
| POST | `/api/competitions` | 201 CreateCompetitionResponse | 400 `INVALID_INPUT` |
| GET | `/api/competitions/{id}` | 200 FindCompetitionResponse | 404 `COMPETITION_NOT_FOUND` |
| POST | `/api/competitions/{id}/publications` | 200 FindCompetitionResponse | 409 `COMPETITION_ALREADY_PUBLISHED` |
| PATCH | `/api/competitions/{id}` | 200 FindCompetitionResponse | 409 `COMPETITION_CANNOT_UPDATE` |
| POST | `/api/competitions/{id}/starts` | 200 FindCompetitionResponse | 400 `COMPETITION_MIN_PARTICIPANTS_NOT_MET` |
| POST | `/api/competitions/{id}/finishes` | 200 FindCompetitionResponse | 409 `COMPETITION_ALREADY_FINISHED` |
| POST | `/api/competitions/{id}/cancellations` | 200 FindCompetitionResponse | 409 `COMPETITION_INVALID_STATUS` |
| DELETE | `/api/competitions/{id}` | 200 FindCompetitionResponse | 404 `COMPETITION_NOT_FOUND` |
| GET | `/api/competitions` | 200 `Page<FindCompetitionResponse>` | 400 `INVALID_INPUT` |
| GET | `/api/competitions/{id}/change-notices` | 200 List | 404 `COMPETITION_NOT_FOUND` |
| POST | `/api/competitions/{competitionId}/participants` | 201 Participant | 409 `COMPETITION_ALREADY_REGISTERED` |
| DELETE | `/api/competitions/{competitionId}/participants` | 200 Participant | 404 `COMPETITION_PARTICIPANT_NOT_FOUND` |
| GET | `/api/competitions/{competitionId}/participants` | 200 List | 404 `COMPETITION_NOT_FOUND` |

```java
@Tag(name = "Competition", description = "대회 생성·조회·상태전환 / 참가 신청·취소")
public interface CompetitionControllerDocs {

    @Operation(summary = "대회 생성 (관리자)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "대회 생성 성공",
            content = @Content(examples = @ExampleObject(value = """
                {
                  "competitionId": "550e8400-...",
                  "name": "2026 봄 모의투자 대회",
                  "type": "MOCK_INVESTMENT",
                  "status": "DRAFT",
                  "firstSeed": 10000000,
                  "registerStartAt": "2026-05-01T00:00:00",
                  "registerEndAt": "2026-05-20T23:59:59",
                  "competitionStartAt": "2026-06-01T09:00:00",
                  "competitionEndAt": "2026-06-30T15:30:00"
                }"""))),
        @ApiResponse(responseCode = "400", description = "유효성 검사 실패",
            content = @Content(examples = @ExampleObject(value = """
                {"status":400,"code":"INVALID_INPUT","message":"대회 이름은 필수입니다.","data":null}""")))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CreateCompetitionResponse createCompetition(@RequestBody @Valid CreateCompetitionRequest request);

    @Operation(summary = "대회 참가 신청",
        parameters = @Parameter(name = "competitionId", description = "대회 UUID", required = true))
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "참가 신청 성공",
            content = @Content(examples = @ExampleObject(value = """
                {
                  "participantId": "abc12345-...",
                  "competitionId": "550e8400-...",
                  "userId": "user-uuid-...",
                  "nickname": "투자왕",
                  "registeredAt": "2026-05-11T14:00:00"
                }"""))),
        @ApiResponse(responseCode = "409", description = "이미 신청한 대회",
            content = @Content(examples = @ExampleObject(value = """
                {"status":409,"code":"COMPETITION_ALREADY_REGISTERED","message":"이미 신청한 대회입니다.","data":null}""")))
    })
    @PostMapping("/{competitionId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    FindCompetitionParticipantResponse registerCompetition(
        @PathVariable UUID competitionId,
        @RequestBody @Valid JoinCompetitionRequest request);

    // 나머지 엔드포인트 동일 패턴
    ...
}
```

---

### 2-5. ranking-service

#### RankingControllerDocs

| 메서드 | 엔드포인트 | 성공 | 실패 |
|---|---|---|---|
| GET | `/api/rankings/competitions/{competitionId}/users/{userId}` | 200 MyRankingResponse | 404 `RANKING_NOT_FOUND` |
| POST | `/api/rankings/competitions/{competitionId}/finalize` | 200 FinalizeRankingsResponse | 409 `RANKING_ALREADY_FINALIZED` |
| GET | `/api/rankings/competitions/{competitionId}` | 200 `List<RankingResponse>` | 404 `COMPETITION_NOT_FOUND` |

```java
@Tag(name = "Ranking", description = "대회 랭킹 조회 / 최종 순위 확정")
public interface RankingControllerDocs {

    @Operation(summary = "내 순위 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(examples = @ExampleObject(value = """
                {
                  "competitionId": "550e8400-...",
                  "userId": "user-uuid-...",
                  "rank": 3,
                  "returnRate": 12.54,
                  "totalAsset": 11254000,
                  "nickname": "투자왕",
                  "isFinalized": false
                }"""))),
        @ApiResponse(responseCode = "404", description = "랭킹 정보 없음",
            content = @Content(examples = @ExampleObject(value = """
                {"status":404,"code":"RANKING_NOT_FOUND","message":"랭킹 정보를 찾을 수 없습니다.","data":null}""")))
    })
    @GetMapping("/competitions/{competitionId}/users/{userId}")
    MyRankingResponse findMyRanking(
        @PathVariable UUID competitionId,
        @PathVariable UUID userId);

    ...
}
```

---

### 2-6. assistant-service

#### ChatControllerDocs

| 메서드 | 엔드포인트 | 성공 | 실패 |
|---|---|---|---|
| POST | `/api/assistants/sessions` | 201 ChatSessionResponse | 403 `FORBIDDEN` |
| GET | `/api/assistants/sessions` | 200 ChatSessionListResponse | 403 `FORBIDDEN` |
| GET | `/api/assistants/sessions/{chatSessionId}/messages` | 200 List | 404 `SESSION_NOT_FOUND` |
| POST | `/api/assistants/sessions/{chatSessionId}/messages` | 201 SendMessageResponse | 400 `MESSAGE_TOO_LONG` |

```java
@Tag(name = "Assistant", description = "AI 챗 세션 생성·조회 / 메시지 송수신")
public interface ChatControllerDocs {

    @Operation(summary = "메시지 전송 (RAG)",
        description = "챗 세션에 메시지를 전송하고 RAG 기반 AI 답변을 받습니다. 최대 2000자.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "답변 생성 성공",
            content = @Content(examples = @ExampleObject(value = """
                {
                  "status": 201, "code": "SUCCESS", "message": "답변이 생성되었습니다.",
                  "data": {
                    "messageId": "msg-uuid-...",
                    "role": "ASSISTANT",
                    "content": "삼성전자는 현재 반도체 업황 회복으로...",
                    "createdAt": "2026-05-11T15:00:00"
                  }
                }"""))),
        @ApiResponse(responseCode = "400", description = "메시지 길이 초과",
            content = @Content(examples = @ExampleObject(value = """
                {"status":400,"code":"MESSAGE_TOO_LONG","message":"메시지는 2000자를 초과할 수 없습니다.","data":null}""")))
    })
    @PostMapping("/{chatSessionId}/messages")
    ResponseEntity<ApiResponse<SendMessageResponse>> sendMessage(
        @Parameter(description = "X-User-Role", in = ParameterIn.HEADER) @RequestHeader("X-User-Role") String role,
        @Parameter(description = "X-User-Id", in = ParameterIn.HEADER) @RequestHeader("X-User-Id") UUID userId,
        @PathVariable UUID chatSessionId,
        @RequestBody SendMessageRequest request);

    ...
}
```

---

### 2-7. notification-service

> 내부/인프라 용도 (Prometheus AlertManager, Slack 인터랙션)이므로 Swagger는 **내부 문서 목적**으로만 추가합니다.

#### NotificationControllerDocs

| 메서드 | 엔드포인트 | 성공 | 실패 |
|---|---|---|---|
| POST | `/api/notifications/prometheus` | 200 void | 401 (secret 불일치) |
| POST | `/api/notifications/interactions` | 200 void | 200 (파싱 실패도 200 반환, Slack 정책) |

---

## 3. 구현 순서 (권장)

```
Step 1.  공통 의존성 추가 (build.gradle × 7)
Step 2.  SwaggerConfig 생성 (× 7, 서비스명만 변경)
Step 3.  user-service — AuthControllerDocs, UserControllerDocs, AdminUserControllerDocs
Step 4.  asset-service — AccountControllerDocs, AssetControllerDocs, HoldingControllerDocs
Step 5.  trade-service — TradeControllerDocs, StockControllerDocs, MarketStatusControllerDocs
Step 6.  competition-service — CompetitionControllerDocs
Step 7.  ranking-service — RankingControllerDocs
Step 8.  assistant-service — ChatControllerDocs, DocumentControllerDocs
Step 9.  notification-service — NotificationControllerDocs
Step 10. 각 서비스 기동 후 http://localhost:{port}/swagger-ui.html 확인
```

---

## 4. 주의 사항

### 4-1. `@RequestMapping` 위치

인터페이스에 `@RequestMapping`을 두면 Spring이 인터페이스도 빈으로 등록하려 할 수 있습니다.  
→ **`@RequestMapping`은 반드시 Controller 클래스에만 선언**하고, 인터페이스 메서드에는 `@PostMapping` / `@GetMapping` 등 메서드 레벨 매핑만 선언합니다.

### 4-2. `@Valid` 위치

`@Valid`는 인터페이스 메서드 시그니처와 컨트롤러 구현 메서드 양쪽에 쓸 수 있지만,  
**컨트롤러 구현부에만 두는 것이 안전**합니다 (프록시 체인 이슈 방지).

### 4-3. 커스텀 파라미터 리졸버 (`@LoginAccount` 등)

Swagger가 인식하지 못하는 커스텀 어노테이션은 인터페이스 파라미터에 `@Parameter(hidden = true)`를 추가해 UI에서 숨깁니다.

### 4-4. `springdoc` + `spring-cloud-gateway` 충돌

api-gateway에는 Swagger를 추가하지 않습니다. Gateway에서 각 서비스 Swagger를 aggregation 하고 싶다면 `springdoc-openapi-starter-webflux-ui`를 별도로 구성해야 합니다 (현재 계획 범위 외).

### 4-5. `application.yml` 설정 (선택)

```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  api-docs:
    path: /v3/api-docs
```

---

## 5. 파일 생성 목록 (전체)

| 서비스 | 생성 파일 |
|---|---|
| user-service | `SwaggerConfig`, `AuthControllerDocs`, `UserControllerDocs`, `AdminUserControllerDocs` |
| asset-service | `SwaggerConfig`, `AccountControllerDocs`, `AssetControllerDocs`, `HoldingControllerDocs` |
| trade-service | `SwaggerConfig`, `TradeControllerDocs`, `StockControllerDocs`, `MarketStatusControllerDocs` |
| competition-service | `SwaggerConfig`, `CompetitionControllerDocs` |
| ranking-service | `SwaggerConfig`, `RankingControllerDocs` |
| assistant-service | `SwaggerConfig`, `ChatControllerDocs`, `DocumentControllerDocs` |
| notification-service | `SwaggerConfig`, `NotificationControllerDocs` |
| **합계** | **SwaggerConfig × 7 + Docs 인터페이스 × 16** |

수정이 필요한 기존 파일 (implements 추가):

| 서비스 | 수정 파일 |
|---|---|
| user-service | `AuthController`, `UserController`, `AdminUserController` |
| asset-service | `AccountController`, `AssetController`, `HoldingController` |
| trade-service | `TradeController`, `StockController`, `MarketStatusController` |
| competition-service | `CompetitionController` |
| ranking-service | `RankingController` |
| assistant-service | `ChatController`, `DocumentController` |
| notification-service | `NotificationController` |
