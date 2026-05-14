package io.antcamp.assistantservice.presentation.controller;

import common.dto.CommonResponse;
import io.antcamp.assistantservice.application.dto.command.RunEvaluationCommand;
import io.antcamp.assistantservice.application.dto.query.GetEvalResultsQuery;
import io.antcamp.assistantservice.application.dto.result.EvalResultListResult;
import io.antcamp.assistantservice.application.service.EvalApplicationService;
import io.antcamp.assistantservice.application.service.QuestionGenerationService;
import io.antcamp.assistantservice.domain.model.EvalQuestion;
import io.antcamp.assistantservice.domain.model.EvalRunStatus;
import io.antcamp.assistantservice.infrastructure.security.ManagerRoleGuard;
import io.antcamp.assistantservice.presentation.dto.request.EvalQuestionRequest;
import io.antcamp.assistantservice.presentation.dto.request.GenerateQuestionsRequest;
import io.antcamp.assistantservice.presentation.dto.request.RunEvaluationRequest;
import io.antcamp.assistantservice.presentation.dto.response.EvalResultListResponse;
import io.antcamp.assistantservice.presentation.dto.response.GeneratedQuestionsResponse;
import io.antcamp.assistantservice.presentation.dto.response.RunEvaluationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assistants/evaluations")
@RequiredArgsConstructor
public class EvalController {

    private final EvalApplicationService evalApplicationService;
    private final QuestionGenerationService questionGenerationService;
    private final ManagerRoleGuard managerRoleGuard;

    @PostMapping
    public ResponseEntity<CommonResponse<RunEvaluationResponse>> runEvaluation(
            @RequestHeader("X-Role") String role,
            @Valid @RequestBody RunEvaluationRequest request
    ) {
        managerRoleGuard.require(role);
        UUID runId = evalApplicationService.runEvaluation(
                new RunEvaluationCommand(
                        request.questions().stream().map(EvalQuestionRequest::toDomain).toList(),
                        request.judgeModels(),
                        request.promptVersionId(),
                        request.ragModel(),
                        request.memo()));
        return CommonResponse.created("평가 파이프라인이 시작되었습니다.", new RunEvaluationResponse(runId));
    }

    @GetMapping("/{evalRunId}/status")
    public ResponseEntity<CommonResponse<EvalRunStatus>> getRunStatus(
            @RequestHeader("X-Role") String role,
            @PathVariable UUID evalRunId
    ) {
        managerRoleGuard.require(role);
        return CommonResponse.ok(evalApplicationService.getRunStatus(evalRunId));
    }

    @PostMapping("/questions/generate")
    public ResponseEntity<CommonResponse<GeneratedQuestionsResponse>> generateQuestions(
            @RequestHeader("X-Role") String role,
            @Valid @RequestBody GenerateQuestionsRequest request
    ) {
        managerRoleGuard.require(role);
        List<EvalQuestion> questions = questionGenerationService.generateQuestions(request.count());
        return CommonResponse.ok("질문 자동 생성 완료.", GeneratedQuestionsResponse.from(questions));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<EvalResultListResponse>> getEvalResults(
            @RequestHeader("X-Role") String role,
            @RequestParam(required = false) String judgeModel,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastUpdatedAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        managerRoleGuard.require(role);
        EvalResultListResult result = evalApplicationService.getEvalResults(
                new GetEvalResultsQuery(judgeModel, lastUpdatedAt, startDate, endDate));
        return CommonResponse.ok(EvalResultListResponse.from(result));
    }
}