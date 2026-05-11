package io.antcamp.assetservice.presentation.controller.docs;

import common.dto.CommonResponse;
import io.antcamp.assetservice.application.dto.command.CreateAccountCommand;
import io.antcamp.assetservice.application.dto.query.AccountResult;
import io.antcamp.assetservice.presentation.dto.response.AccountResponse;
import io.antcamp.assetservice.presentation.dto.response.BalanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Account", description = "계좌 생성 / 입금 / 출금 / 조회")
public interface AccountControllerDocs {

    @Operation(summary = "계좌 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "계좌 생성 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 201,
                                      "code": "SUCCESS",
                                      "message": "계좌 생성에 성공했습니다.",
                                      "data": {
                                        "accountId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 400,
                                      "code": "INVALID_INPUT",
                                      "message": "입력값이 유효하지 않습니다.",
                                      "data": null
                                    }""")))
    })
    @PostMapping
    ResponseEntity<CommonResponse<AccountResponse>> createAccount(@RequestBody CreateAccountCommand command);

    @Operation(summary = "입금",
            parameters = @Parameter(name = "accountId", description = "계좌 UUID", in = ParameterIn.PATH, required = true))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "입금 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "입금에 성공했습니다.",
                                      "data": {
                                        "accountId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                        "balance": 1500000
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "400", description = "금액이 0 이하",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 400,
                                      "code": "INVALID_INPUT_VALUE",
                                      "message": "요청 파라미터 오류입니다.",
                                      "data": null
                                    }""")))
    })
    @PostMapping("/{accountId}/deposit")
    ResponseEntity<CommonResponse<BalanceResponse>> deposit(
            @PathVariable UUID accountId,
            @RequestParam Long amount);

    @Operation(summary = "출금",
            parameters = @Parameter(name = "accountId", description = "계좌 UUID", in = ParameterIn.PATH, required = true))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출금 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "출금에 성공했습니다.",
                                      "data": {
                                        "accountId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                        "balance": 500000
                                      }
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
    @PostMapping("/{accountId}/withdraw")
    ResponseEntity<CommonResponse<BalanceResponse>> withdraw(
            @PathVariable UUID accountId,
            @RequestParam Long amount);

    @Operation(summary = "계좌 단건 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "요청에 성공했습니다.",
                                      "data": {
                                        "accountId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                        "userId": "550e8400-e29b-41d4-a716-446655440000",
                                        "balance": 1000000
                                      }
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
    @GetMapping("/{accountId}")
    ResponseEntity<CommonResponse<AccountResult>> getAccount(
            @PathVariable UUID accountId,
            @Parameter(description = "게이트웨이가 주입하는 사용자 ID", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Id") UUID userId);

    @Operation(summary = "내 계좌 목록 조회")
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
                                          "accountId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                          "userId": "550e8400-e29b-41d4-a716-446655440000",
                                          "balance": 1000000
                                        }
                                      ]
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "계좌 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 404,
                                      "code": "ASSET_SERVICE_ERROR",
                                      "message": "자산 서비스 에러입니다.",
                                      "data": null
                                    }""")))
    })
    @GetMapping
    ResponseEntity<CommonResponse<List<AccountResult>>> getMyAccounts(
            @Parameter(description = "게이트웨이가 주입하는 사용자 ID", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Id") UUID userId);
}
