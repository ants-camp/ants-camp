package io.antcamp.assistantservice.presentation.controller;

import common.dto.CommonResponse;
import io.antcamp.assistantservice.application.dto.command.SendMessageCommand;
import io.antcamp.assistantservice.application.dto.result.ChatMessageResult;
import io.antcamp.assistantservice.application.dto.result.ChatSessionResult;
import io.antcamp.assistantservice.application.service.ChatApplicationService;
import io.antcamp.assistantservice.application.service.RagApplicationService;
import io.antcamp.assistantservice.domain.model.CursorSlice;
import io.antcamp.assistantservice.infrastructure.security.PlayerRoleGuard;
import io.antcamp.assistantservice.presentation.dto.request.SendMessageRequest;
import io.antcamp.assistantservice.presentation.dto.response.ChatMessageResponse;
import io.antcamp.assistantservice.presentation.dto.response.ChatSessionListResponse;
import io.antcamp.assistantservice.presentation.dto.response.ChatSessionResponse;
import io.antcamp.assistantservice.presentation.dto.response.SendMessageResponse;
import jakarta.validation.Valid;
import io.antcamp.assistantservice.presentation.controller.docs.ChatControllerDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assistants/sessions")
@RequiredArgsConstructor
public class ChatController implements ChatControllerDocs {

    private final ChatApplicationService chatApplicationService;
    private final RagApplicationService ragApplicationService;
    private final PlayerRoleGuard playerRoleGuard;

    @PostMapping
    public ResponseEntity<CommonResponse<ChatSessionResponse>> createSession(
            @RequestHeader("X-Role") String role,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        playerRoleGuard.require(role);
        ChatSessionResult result = chatApplicationService.createSession(userId);
        return CommonResponse.created("세션이 생성되었습니다.", ChatSessionResponse.from(result));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<ChatSessionListResponse>> getSessions(
            @RequestHeader("X-Role") String role,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastUpdatedAt
    ) {
        playerRoleGuard.require(role);
        CursorSlice<ChatSessionResult, LocalDateTime> slice =
                chatApplicationService.getSessions(userId, keyword, lastUpdatedAt);
        return CommonResponse.ok(ChatSessionListResponse.from(slice));
    }

    @GetMapping("/{chatSessionId}/messages")
    public ResponseEntity<CommonResponse<List<ChatMessageResponse>>> getMessages(
            @RequestHeader("X-Role") String role,
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID chatSessionId
    ) {
        playerRoleGuard.require(role);
        List<ChatMessageResult> results = chatApplicationService.getMessages(chatSessionId, userId);
        List<ChatMessageResponse> responses = results.stream().map(ChatMessageResponse::from).toList();
        return CommonResponse.ok(responses);
    }

    @PostMapping("/{chatSessionId}/messages")
    public ResponseEntity<CommonResponse<SendMessageResponse>> sendMessage(
            @RequestHeader("X-Role") String role,
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID chatSessionId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        playerRoleGuard.require(role);
        SendMessageCommand command = new SendMessageCommand(chatSessionId, userId, request.content());
        SendMessageResponse response = SendMessageResponse.from(ragApplicationService.sendMessage(command));
        return CommonResponse.created("답변이 생성되었습니다.", response);
    }
}