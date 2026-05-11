package io.antcamp.tradeservice.presentation.docs;

import io.antcamp.tradeservice.infrastructure.dto.StockSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Stock", description = "종목 검색 / 실시간 체결·호가 구독 관리")
public interface StockControllerDocs {

    @Operation(summary = "종목 검색", description = "종목명 또는 종목코드로 국내 주식 종목을 검색합니다. 검색어 생략 시 전체 반환.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    [
                                      {
                                        "stockCode": "005930",
                                        "stockName": "삼성전자",
                                        "market": "KOSPI"
                                      },
                                      {
                                        "stockCode": "000660",
                                        "stockName": "SK하이닉스",
                                        "market": "KOSPI"
                                      }
                                    ]"""))),
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
    @GetMapping("/search")
    ResponseEntity<List<StockSearchResponse>> search(
            @Parameter(description = "검색어 (종목명 또는 종목코드, 생략 시 전체)")
            @RequestParam(defaultValue = "") String q,
            @Parameter(description = "최대 결과 수 (기본 20)")
            @RequestParam(defaultValue = "20") int limit);

    @Operation(summary = "실시간 체결·호가 구독 등록",
            description = """
                    특정 종목의 체결가(H0STCNT0)와 호가(H0STASP0)를 동시에 구독합니다.
                    구독 후 STOMP 클라이언트는 아래 토픽을 구독하면 실시간 데이터를 수신합니다:
                    - `/topic/price/{stockCode}` — 체결가
                    - `/topic/orderbook/{stockCode}` — 호가
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구독 등록 성공",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(name = "성공", value = "005930 실시간 구독 완료 (체결 + 호가)"))),
            @ApiResponse(responseCode = "503", description = "KIS WebSocket 연결 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 503,
                                      "code": "KIS_SERVICE_ERROR",
                                      "message": "KIS 서비스 에러입니다.",
                                      "data": null
                                    }""")))
    })
    @PostMapping("/realtime/{stockCode}")
    ResponseEntity<String> subscribe(
            @Parameter(description = "종목 코드 (예: 005930)", required = true)
            @PathVariable String stockCode);

    @Operation(summary = "실시간 체결·호가 구독 해제", description = "체결가와 호가 구독을 일괄 해제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구독 해제 성공",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(name = "성공", value = "005930 실시간 구독 해제 완료"))),
            @ApiResponse(responseCode = "503", description = "KIS WebSocket 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 503,
                                      "code": "KIS_SERVICE_ERROR",
                                      "message": "KIS 서비스 에러입니다.",
                                      "data": null
                                    }""")))
    })
    @DeleteMapping("/realtime/{stockCode}")
    ResponseEntity<String> unsubscribe(
            @Parameter(description = "종목 코드 (예: 005930)", required = true)
            @PathVariable String stockCode);
}
