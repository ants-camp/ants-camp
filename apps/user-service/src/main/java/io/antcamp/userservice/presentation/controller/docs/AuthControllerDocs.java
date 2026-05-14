package io.antcamp.userservice.presentation.controller.docs;

import common.dto.CommonResponse;
import io.antcamp.userservice.presentation.dto.request.LoginRequest;
import io.antcamp.userservice.presentation.dto.request.LogoutRequest;
import io.antcamp.userservice.presentation.dto.request.ReissueRequest;
import io.antcamp.userservice.presentation.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "로그인 / 로그아웃 / 토큰 재발급")
public interface AuthControllerDocs {

    @Operation(summary = "로그인", description = "이메일·비밀번호로 로그인하고 JWT를 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "로그인에 성공했습니다.",
                                      "data": {
                                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
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
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 401,
                                      "code": "LOGIN_FAILED",
                                      "message": "아이디 또는 비밀번호가 올바르지 않습니다.",
                                      "data": null
                                    }""")))
    })
    @PostMapping("/login")
    ResponseEntity<CommonResponse<LoginResponse>> login(@RequestBody LoginRequest request);

    @Operation(summary = "로그아웃", description = "액세스 토큰을 블랙리스트에 등록하여 무효화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "로그아웃에 성공했습니다.",
                                      "data": null
                                    }"""))),
            @ApiResponse(responseCode = "401", description = "이미 로그아웃된 토큰",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 401,
                                      "code": "TOKEN_BLACKLISTED",
                                      "message": "이미 로그아웃된 토큰입니다.",
                                      "data": null
                                    }""")))
    })
    @PostMapping("/logout")
    ResponseEntity<CommonResponse<Void>> logout(@RequestBody LogoutRequest request);

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새 Access Token을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "토큰 재발급에 성공했습니다.",
                                      "data": {
                                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                        "user": {
                                          "userId": "550e8400-e29b-41d4-a716-446655440000",
                                          "email": "user@example.com",
                                          "name": "홍길동",
                                          "role": "PLAYER",
                                          "phone": "010-1234-5678"
                                        }
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 401,
                                      "code": "INVALID_REFRESH_TOKEN",
                                      "message": "유효하지 않은 리프레시 토큰입니다.",
                                      "data": null
                                    }""")))
    })
    @PostMapping("/reissue")
    ResponseEntity<CommonResponse<LoginResponse>> reissue(@RequestBody ReissueRequest request);
}
