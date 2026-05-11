package io.antcamp.userservice.presentation.controller.docs;

import common.dto.CommonResponse;
import io.antcamp.userservice.presentation.dto.request.UserRegisterRequest;
import io.antcamp.userservice.presentation.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@Tag(name = "User", description = "회원가입 / 내 정보 조회")
public interface UserControllerDocs {

    @Operation(summary = "회원가입", description = "이메일·비밀번호·이름·전화번호로 회원가입합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 201,
                                      "code": "SUCCESS",
                                      "message": "회원가입에 성공했습니다.",
                                      "data": {
                                        "userId": "550e8400-e29b-41d4-a716-446655440000",
                                        "email": "user@example.com",
                                        "name": "홍길동",
                                        "role": "PLAYER",
                                        "phone": "010-1234-5678"
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
    @PostMapping("/register")
    ResponseEntity<CommonResponse<UserResponse>> register(@RequestBody UserRegisterRequest request);

    @Operation(summary = "내 정보 조회", description = "게이트웨이가 주입한 X-User-Id 헤더로 내 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "내 정보 조회에 성공했습니다.",
                                      "data": {
                                        "userId": "550e8400-e29b-41d4-a716-446655440000",
                                        "email": "user@example.com",
                                        "name": "홍길동",
                                        "role": "PLAYER",
                                        "phone": "010-1234-5678"
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
    @GetMapping("/me")
    ResponseEntity<CommonResponse<UserResponse>> me(
            @Parameter(description = "게이트웨이가 주입하는 사용자 ID", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Id") UUID userId);
}
