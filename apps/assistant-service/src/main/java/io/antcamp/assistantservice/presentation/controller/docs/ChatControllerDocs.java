package io.antcamp.assistantservice.presentation.controller.docs;

import common.dto.CommonResponse;
import io.antcamp.assistantservice.presentation.dto.request.SendMessageRequest;
import io.antcamp.assistantservice.presentation.dto.response.ChatMessageResponse;
import io.antcamp.assistantservice.presentation.dto.response.ChatSessionListResponse;
import io.antcamp.assistantservice.presentation.dto.response.ChatSessionResponse;
import io.antcamp.assistantservice.presentation.dto.response.SendMessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name = "Assistant - Chat", description = "AI 챗 세션 생성·조회 / 메시지 송수신 (RAG)")
public interface ChatControllerDocs {

    @Operation(summary = "챗 세션 생성", description = "새 AI 챗 세션을 생성합니다. PLAYER 권한 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "세션 생성 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 201,
                                      "code": "SUCCESS",
                                      "message": "세션이 생성되었습니다.",
                                      "data": {
                                        "sessionId": "session-uuid-...",
                                        "userId": "550e8400-e29b-41d4-a716-446655440000",
                                        "createdAt": "2026-05-11T14:00:00"
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "PLAYER 권한 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 403,
                                      "code": "FORBIDDEN",
                                      "message": "접근 권한이 없습니다.",
                                      "data": null
                                    }""")))
    })
    @PostMapping
    ResponseEntity<CommonResponse<ChatSessionResponse>> createSession(
            @Parameter(description = "X-User-Role", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Role") String role,
            @Parameter(description = "X-User-Id", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Id") UUID userId);

    @Operation(summary = "내 챗 세션 목록 조회", description = "키워드 및 커서 기반 페이지네이션을 지원합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "요청에 성공했습니다.",
                                      "data": {
                                        "sessions": [
                                          {
                                            "sessionId": "session-uuid-...",
                                            "lastMessage": "삼성전자 전망은?",
                                            "updatedAt": "2026-05-11T15:00:00"
                                          }
                                        ],
                                        "hasNext": false,
                                        "nextCursor": null
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "PLAYER 권한 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":403,"code":"FORBIDDEN","message":"접근 권한이 없습니다.","data":null}""")))
    })
    @GetMapping
    ResponseEntity<CommonResponse<ChatSessionListResponse>> getSessions(
            @Parameter(description = "X-User-Role", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Role") String role,
            @Parameter(description = "X-User-Id", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Id") UUID userId,
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "커서 (마지막 updatedAt, ISO_DATE_TIME)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastUpdatedAt);

    @Operation(summary = "세션 메시지 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "요청에 성공했습니다.",
                                      "data": [
                                        {"messageId":"msg-001","role":"USER","content":"삼성전자 전망은?","createdAt":"2026-05-11T14:01:00"},
                                        {"messageId":"msg-002","role":"ASSISTANT","content":"삼성전자는 반도체 업황 회복으로...","createdAt":"2026-05-11T14:01:05"}
                                      ]
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 세션",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":404,"code":"SESSION_NOT_FOUND","message":"존재하지 않는 세션입니다.","data":null}""")))
    })
    @GetMapping("/{chatSessionId}/messages")
    ResponseEntity<CommonResponse<List<ChatMessageResponse>>> getMessages(
            @Parameter(description = "X-User-Role", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Role") String role,
            @Parameter(description = "X-User-Id", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Id") UUID userId,
            @Parameter(description = "챗 세션 UUID", required = true) @PathVariable UUID chatSessionId);

    @Operation(summary = "메시지 전송 (RAG)",
            description = "챗 세션에 메시지를 전송하고 RAG 기반 AI 답변을 받습니다. 최대 2000자.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "답변 생성 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 201,
                                      "code": "SUCCESS",
                                      "message": "답변이 생성되었습니다.",
                                      "data": {
                                        "messageId": "msg-uuid-...",
                                        "role": "ASSISTANT",
                                        "content": "삼성전자는 현재 반도체 업황 회복으로 긍정적인 전망이 예상됩니다...",
                                        "createdAt": "2026-05-11T15:00:05"
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "400", description = "메시지 길이 초과 (2000자 이상)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 400,
                                      "code": "MESSAGE_TOO_LONG",
                                      "message": "메시지는 2000자를 초과할 수 없습니다.",
                                      "data": null
                                    }""")))
    })
    @PostMapping("/{chatSessionId}/messages")
    ResponseEntity<CommonResponse<SendMessageResponse>> sendMessage(
            @Parameter(description = "X-User-Role", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Role") String role,
            @Parameter(description = "X-User-Id", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Id") UUID userId,
            @Parameter(description = "챗 세션 UUID", required = true) @PathVariable UUID chatSessionId,
            @RequestBody SendMessageRequest request);
}
