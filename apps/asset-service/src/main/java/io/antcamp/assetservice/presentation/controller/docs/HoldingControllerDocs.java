package io.antcamp.assetservice.presentation.controller.docs;

import common.dto.CommonResponse;
import io.antcamp.assetservice.application.dto.command.BuyHoldingCommand;
import io.antcamp.assetservice.application.dto.command.SellHoldingCommand;
import io.antcamp.assetservice.application.dto.query.TradeResult;
import io.antcamp.assetservice.presentation.dto.response.HoldingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Holding", description = "주식 매수 / 매도 / 보유 종목 조회")
public interface HoldingControllerDocs {

    @Operation(summary = "주식 매수", description = "지정한 계좌로 주식을 매수합니다. 잔액이 부족하면 실패합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "매수 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "accountId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                      "stockCode": "005930",
                                      "quantity": 10,
                                      "price": 75000.0,
                                      "totalAmount": 750000,
                                      "remainBalance": 250000
                                    }"""))),
            @ApiResponse(responseCode = "409", description = "잔액 부족",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 409,
                                      "code": "ASSET_SERVICE_ERROR",
                                      "message": "잔액이 부족합니다.",
                                      "data": null
                                    }""")))
    })
    @PostMapping("/buy")
    ResponseEntity<TradeResult> buy(
            @RequestBody BuyHoldingCommand command,
            @Parameter(description = "게이트웨이가 주입하는 사용자 ID", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Id") UUID userId);

    @Operation(summary = "주식 매도", description = "보유 중인 주식을 매도합니다. 보유 수량 초과 시 실패합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "매도 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "accountId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                      "stockCode": "005930",
                                      "quantity": 5,
                                      "price": 76000.0,
                                      "totalAmount": 380000,
                                      "remainBalance": 1380000
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "보유 종목 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 404,
                                      "code": "ASSET_SERVICE_ERROR",
                                      "message": "보유 종목을 찾을 수 없습니다.",
                                      "data": null
                                    }""")))
    })
    @PostMapping("/sell")
    ResponseEntity<TradeResult> sell(
            @RequestBody SellHoldingCommand command,
            @Parameter(description = "게이트웨이가 주입하는 사용자 ID", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Id") UUID userId);

    @Operation(summary = "보유 종목 목록 조회")
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
                                          "stockCode": "005930",
                                          "stockName": "삼성전자",
                                          "quantity": 10,
                                          "avgPrice": 74000.0,
                                          "currentPrice": 75000.0,
                                          "evaluationAmount": 750000,
                                          "profitRate": 1.35
                                        }
                                      ]
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "타인의 계좌 접근",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 403,
                                      "code": "FORBIDDEN",
                                      "message": "접근 권한이 없습니다.",
                                      "data": null
                                    }""")))
    })
    @GetMapping
    ResponseEntity<CommonResponse<List<HoldingResponse>>> getHoldings(
            @Parameter(description = "계좌 UUID", required = true)
            @RequestParam UUID accountId,
            @Parameter(description = "게이트웨이가 주입하는 사용자 ID", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Id") UUID userId);
}
