package io.antcamp.assistantservice.presentation.controller;

import common.dto.ApiResponse;
import io.antcamp.assistantservice.application.dto.command.RunPairwiseCommand;
import io.antcamp.assistantservice.application.service.PairwiseApplicationService;
import io.antcamp.assistantservice.domain.model.PairwiseSummary;
import io.antcamp.assistantservice.infrastructure.security.ManagerRoleGuard;
import io.antcamp.assistantservice.presentation.dto.request.RunPairwiseRequest;
import io.antcamp.assistantservice.presentation.dto.response.PairwiseSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/assistants/evaluations/pairwise")
@RequiredArgsConstructor
public class PairwiseController {

    private final PairwiseApplicationService pairwiseApplicationService;
    private final ManagerRoleGuard managerRoleGuard;

    // Pairwise 비교 실행
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> runPairwise(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody RunPairwiseRequest request
    ) {
        managerRoleGuard.require(role);
        pairwiseApplicationService.runPairwise(
                new RunPairwiseCommand(request.evalRunIdA(), request.evalRunIdB(), request.judgeModels()));
        return ApiResponse.accepted("Pairwise 비교가 시작되었습니다.", null);
    }

    // Pairwise 결과 집계 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PairwiseSummaryResponse>> getSummary(
            @RequestHeader("X-User-Role") String role,
            @RequestParam UUID evalRunIdA,
            @RequestParam UUID evalRunIdB
    ) {
        managerRoleGuard.require(role);
        PairwiseSummary summary = pairwiseApplicationService.getSummary(evalRunIdA, evalRunIdB);
        return ApiResponse.ok(PairwiseSummaryResponse.from(summary));
    }
}