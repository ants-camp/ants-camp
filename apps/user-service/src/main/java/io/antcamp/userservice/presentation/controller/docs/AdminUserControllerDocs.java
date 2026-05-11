package io.antcamp.userservice.presentation.controller.docs;

import common.dto.CommonResponse;
import io.antcamp.userservice.presentation.dto.request.UserRegisterRequest;
import io.antcamp.userservice.presentation.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Admin - User", description = "관리자 전용: 매니저 계정 생성 / 전체 사용자 조회")
public interface AdminUserControllerDocs {

    @Operation(summary = "매니저 계정 생성 (관리자 전용)", description = "ADMIN 권한으로 MANAGER 역할의 계정을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "매니저 계정 생성 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 201,
                                      "code": "SUCCESS",
                                      "message": "매니저 계정 생성에 성공했습니다.",
                                      "data": {
                                        "userId": "550e8400-e29b-41d4-a716-446655440001",
                                        "email": "manager@example.com",
                                        "name": "김매니저",
                                        "role": "MANAGER",
                                        "phone": "010-9876-5432"
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 409,
                                      "code": "DUPLICATE_EMAIL",
                                      "message": "이미 사용 중인 이메일입니다.",
                                      "data": null
                                    }""")))
    })
    @PostMapping("/manager")
    ResponseEntity<CommonResponse<UserResponse>> createManager(@RequestBody UserRegisterRequest request);

    @Operation(summary = "전체 사용자 목록 조회 (관리자 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "전체 사용자 조회에 성공했습니다.",
                                      "data": [
                                        {
                                          "userId": "550e8400-e29b-41d4-a716-446655440000",
                                          "email": "user@example.com",
                                          "name": "홍길동",
                                          "role": "PLAYER",
                                          "phone": "010-1234-5678"
                                        }
                                      ]
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음",
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
    ResponseEntity<CommonResponse<List<UserResponse>>> getAllUsers();
}
