package io.antcamp.userservice.presentation.controller.docs;

import common.dto.CommonResponse;
import io.antcamp.userservice.presentation.dto.response.InternalUserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Tag(name = "Internal - User", description = "서비스 간 내부 통신 전용: 사용자 인증 정보 조회")
public interface InternalUserControllerDocs {

    @Operation(summary = "사용자 인증 정보 조회 (내부 전용)", description = "다른 마이크로서비스에서 userId로 사용자 인증 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "사용자 인증 정보 조회에 성공했습니다.",
                                      "data": {
                                        "userId": "550e8400-e29b-41d4-a716-446655440000",
                                        "email": "user@example.com",
                                        "role": "PLAYER"
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 404,
                                      "code": "USER_NOT_FOUND",
                                      "message": "존재하지 않는 사용자입니다.",
                                      "data": null
                                    }""")))
    })
    @GetMapping("/{userId}")
    ResponseEntity<CommonResponse<InternalUserResponse>> getUser(
            @Parameter(description = "사용자 UUID", required = true)
            @PathVariable UUID userId);
}
