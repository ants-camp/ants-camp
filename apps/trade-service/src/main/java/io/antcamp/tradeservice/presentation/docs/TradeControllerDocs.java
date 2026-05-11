package io.antcamp.tradeservice.presentation.docs;

import common.dto.CommonResponse;
import io.antcamp.tradeservice.infrastructure.dto.AccessTokenResponse;
import io.antcamp.tradeservice.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name = "Trade", description = "KIS 주식 가격 조회 / 주문 접수·취소 / 미체결 목록")
public interface TradeControllerDocs {

    @Operation(summary = "KIS 액세스 토큰 발급", description = "KIS Open API 액세스 토큰을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발급 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "요청에 성공했습니다.",
                                      "data": {
                                        "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGci...",
                                        "tokenType": "Bearer",
                                        "expiresIn": 86400
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "503", description = "KIS 서버 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 503,
                                      "code": "KIS_SERVICE_ERROR",
                                      "message": "KIS 서비스 에러입니다.",
                                      "data": null
                                    }""")))
    })
    @PostMapping("/access-token")
    ResponseEntity<CommonResponse<AccessTokenResponse>> getKisAccessToken();

    @Operation(summary = "분봉 기준 가격 조회", description = "특정 시각 기준 분봉 체결가를 조회합니다. date_time 생략 시 현재 시각 기준.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "요청에 성공했습니다.",
                                      "data": 75400.0
                                    }"""))),
            @ApiResponse(responseCode = "503", description = "KIS 서버 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 503,
                                      "code": "KIS_SERVICE_ERROR",
                                      "message": "KIS 서비스 에러입니다.",
                                      "data": null
                                    }""")))
    })
    @GetMapping("/minute-price")
    ResponseEntity<CommonResponse<Double>> getMinutePrice(
            @Parameter(description = "종목 코드 (예: 005930)", required = true)
            @RequestParam("stock_code") String stockCode,
            @Parameter(description = "기준 일시 (ISO_DATE_TIME, 생략 시 현재)")
            @RequestParam(value = "date_time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime);

    @Operation(summary = "현재가 조회", description = "해당 종목의 현재 체결가를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {"status":200,"code":"SUCCESS","message":"요청에 성공했습니다.","data":75500.0}"""))),
            @ApiResponse(responseCode = "503", description = "KIS 서버 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":503,"code":"KIS_SERVICE_ERROR","message":"KIS 서비스 에러입니다.","data":null}""")))
    })
    @GetMapping("/now-price")
    ResponseEntity<CommonResponse<Double>> getNowPrice(
            @Parameter(description = "종목 코드", required = true) @RequestParam("stock_code") String stockCode,
            @Parameter(description = "기준 일시 (생략 시 현재)")
            @RequestParam(value = "date_time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime);

    @Operation(summary = "분봉 상세 가격 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "요청에 성공했습니다.",
                                      "data": {
                                        "stockCode": "005930",
                                        "price": 75400.0,
                                        "openPrice": 74800.0,
                                        "highPrice": 75600.0,
                                        "lowPrice": 74700.0,
                                        "volume": 12345678
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "503", description = "KIS 서버 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":503,"code":"KIS_SERVICE_ERROR","message":"KIS 서비스 에러입니다.","data":null}""")))
    })
    @GetMapping("/price")
    ResponseEntity<CommonResponse<MinutePriceResponse>> getPrice(
            @Parameter(description = "종목 코드", required = true) @RequestParam("stock_code") String stockCode,
            @Parameter(description = "기준 일시 (생략 시 현재)")
            @RequestParam(value = "date_time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime);

    @Operation(summary = "일봉/주봉/월봉/년봉 차트 조회",
            description = "period: D=일봉 W=주봉 M=월봉 Y=년봉")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "요청에 성공했습니다.",
                                      "data": {
                                        "stockCode": "005930",
                                        "period": "D",
                                        "candles": [
                                          {"date":"20260510","open":74800,"high":75600,"low":74700,"close":75400,"volume":12345678}
                                        ]
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "503", description = "KIS 서버 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":503,"code":"KIS_SERVICE_ERROR","message":"KIS 서비스 에러입니다.","data":null}""")))
    })
    @GetMapping("/chart")
    ResponseEntity<CommonResponse<DailyChartResponse>> getDailyChart(
            @Parameter(description = "종목 코드", required = true) @RequestParam("stock_code") String stockCode,
            @Parameter(description = "시작일 (yyyyMMdd)", required = true) @RequestParam("start_date") String startDate,
            @Parameter(description = "종료일 (yyyyMMdd)", required = true) @RequestParam("end_date") String endDate,
            @Parameter(description = "봉 단위 (D/W/M/Y, 기본값 D)") @RequestParam(value = "period", defaultValue = "D") String period);

    @Operation(summary = "복수 종목 현재가 일괄 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "요청에 성공했습니다.",
                                      "data": {
                                        "prices": [
                                          {"stockCode":"005930","price":75400.0},
                                          {"stockCode":"000660","price":182000.0}
                                        ]
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "503", description = "KIS 서버 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":503,"code":"KIS_SERVICE_ERROR","message":"KIS 서비스 에러입니다.","data":null}""")))
    })
    @PostMapping("/stock-price-list")
    ResponseEntity<CommonResponse<StockPriceList>> stockPriceList(
            @RequestBody StockList stockList,
            @Parameter(description = "기준 일시 (생략 시 현재)")
            @RequestParam(value = "date_time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime);

    @Operation(summary = "주문 접수 (시장가/지정가 통합)",
            description = """
                    시장가·지정가 매수·매도를 통합 처리합니다.
                    - **EXECUTED**: 즉시 체결
                    - **PENDING**: 지정가 조건 미충족, 미체결 대기 (tradeId 반환)
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 접수 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공 (즉시체결)", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "요청에 성공했습니다.",
                                      "data": {
                                        "tradeId": "550e8400-e29b-41d4-a716-446655440000",
                                        "stockCode": "005930",
                                        "stockAmount": 10,
                                        "orderType": "MARKET",
                                        "side": "BUY",
                                        "status": "EXECUTED",
                                        "executedPrice": 75400.0,
                                        "executedAt": "2026-05-11T10:30:00"
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 계좌",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 404,
                                      "code": "TRADE_NOT_FOUND",
                                      "message": "존재하지 않는 매매입니다.",
                                      "data": null
                                    }""")))
    })
    @PostMapping("/order")
    ResponseEntity<CommonResponse<TradeOrderResponse>> placeOrder(
            @RequestBody TradeOrderRequest request,
            @Parameter(hidden = true) UUID accountId);

    @Operation(summary = "미체결 주문 취소", description = "PENDING 상태의 지정가 주문을 취소합니다. 본인 주문만 취소 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "취소 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "요청에 성공했습니다.",
                                      "data": {
                                        "tradeId": "550e8400-e29b-41d4-a716-446655440000",
                                        "status": "CANCELLED"
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "409", description = "이미 처리된 주문",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 409,
                                      "code": "TRADE_ALREADY_PROCESSED",
                                      "message": "이미 처리된 주문 취소 시도 (PENDING이 아닐 때)입니다.",
                                      "data": null
                                    }""")))
    })
    @DeleteMapping("/order/{tradeId}")
    ResponseEntity<CommonResponse<TradeOrderResponse>> cancelOrder(
            @Parameter(description = "취소할 주문 UUID", required = true) @PathVariable UUID tradeId,
            @Parameter(hidden = true) UUID accountId);

    @Operation(summary = "미체결 주문 목록 조회", description = "내 PENDING 상태 주문 전체를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "요청에 성공했습니다.",
                                      "data": [
                                        {
                                          "tradeId": "550e8400-e29b-41d4-a716-446655440000",
                                          "stockCode": "005930",
                                          "stockAmount": 5,
                                          "side": "BUY",
                                          "limitPrice": 74000.0,
                                          "status": "PENDING",
                                          "createdAt": "2026-05-11T09:00:00"
                                        }
                                      ]
                                    }"""))),
            @ApiResponse(responseCode = "400", description = "계좌 정보 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 400,
                                      "code": "INVALID_INPUT",
                                      "message": "입력값이 유효하지 않습니다.",
                                      "data": null
                                    }""")))
    })
    @GetMapping("/pending")
    ResponseEntity<CommonResponse<List<PendingOrderResponse>>> getPendingOrders(
            @Parameter(hidden = true) UUID accountId);
}
