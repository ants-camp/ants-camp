package io.antcamp.assetservice.presentation.controller.docs;

import common.dto.CommonResponse;
import io.antcamp.assetservice.application.dto.query.AssetResult;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Asset", description = "계좌 전체 자산 조회 (현금 + 보유 종목 평가액)")
public interface AssetControllerDocs {

    @Operation(summary = "자산 조회", description = "accountId 기준으로 현금 잔액과 보유 종목 평가액을 합산한 총 자산을 반환합니다.")
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
                                        "cashBalance": 500000,
                                        "holdingsValue": 750000,
                                        "totalAsset": 1250000
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
    @GetMapping
    ResponseEntity<CommonResponse<AssetResult>> getAsset(
            @Parameter(description = "계좌 UUID", required = true)
            @RequestParam UUID accountId,
            @Parameter(description = "게이트웨이가 주입하는 사용자 ID", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Id") UUID userId);
}
