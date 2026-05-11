package io.antcamp.tradeservice.presentation.docs;

import io.antcamp.tradeservice.infrastructure.dto.MarketStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Market", description = "장 운영 상태 조회")
public interface MarketStatusControllerDocs {

    @Operation(summary = "장 운영 상태 조회", description = "현재 국내 주식 시장의 개장 여부, 개장 시각, 휴장 여부를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공 (개장 중)", value = """
                                    {
                                      "status": "OPEN",
                                      "openTime": "09:00",
                                      "closeTime": "15:30",
                                      "isHoliday": false,
                                      "message": "정규 장 운영 중입니다."
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
    @GetMapping("/status")
    ResponseEntity<MarketStatusResponse> getStatus();
}
