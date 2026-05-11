package io.antcamp.assistantservice.presentation.controller;

import common.dto.CommonResponse;
import io.antcamp.assistantservice.application.service.PromptVersionApplicationService;
import io.antcamp.assistantservice.infrastructure.security.ManagerRoleGuard;
import io.antcamp.assistantservice.presentation.dto.request.SavePromptVersionRequest;
import io.antcamp.assistantservice.presentation.dto.response.PromptVersionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assistants/prompt-versions")
@RequiredArgsConstructor
public class PromptVersionController {

    private final PromptVersionApplicationService promptVersionApplicationService;
    private final ManagerRoleGuard managerRoleGuard;

    @PostMapping
    public ResponseEntity<CommonResponse<PromptVersionResponse>> create(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody SavePromptVersionRequest request
    ) {
        managerRoleGuard.require(role);
        PromptVersionResponse response = PromptVersionResponse.from(
                promptVersionApplicationService.save(request.name(), request.content()));
        return CommonResponse.created("프롬프트 버전이 저장되었습니다.", response);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<PromptVersionResponse>>> list(
            @RequestHeader("X-User-Role") String role
    ) {
        managerRoleGuard.require(role);
        List<PromptVersionResponse> responses = promptVersionApplicationService.findAll().stream()
                .map(PromptVersionResponse::from)
                .toList();
        return CommonResponse.ok(responses);
    }
}