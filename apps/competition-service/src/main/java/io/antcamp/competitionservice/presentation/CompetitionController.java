package io.antcamp.competitionservice.presentation;

import io.antcamp.competitionservice.application.CompetitionParticipantService;
import io.antcamp.competitionservice.application.CompetitionService;
import io.antcamp.competitionservice.application.dto.CreateCompetitionCommand;
import io.antcamp.competitionservice.application.dto.JoinCompetitionCommand;
import io.antcamp.competitionservice.application.dto.UpdateCompetitionCommand;
import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import io.antcamp.competitionservice.presentation.dto.request.CreateCompetitionRequest;
import io.antcamp.competitionservice.presentation.dto.request.JoinCompetitionRequest;
import io.antcamp.competitionservice.presentation.dto.request.UpdateCompetitionRequest;
import common.dto.ApiResponse;
import io.antcamp.competitionservice.presentation.dto.response.CreateCompetitionResponse;
import io.antcamp.competitionservice.presentation.dto.response.FindCompetitionChangeNoticeResponse;
import io.antcamp.competitionservice.presentation.dto.response.FindCompetitionParticipantResponse;
import io.antcamp.competitionservice.presentation.dto.response.FindCompetitionResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/competitions")
public class CompetitionController {

    private final CompetitionService competitionService;
    private final CompetitionParticipantService competitionParticipantService;

    // ─── 대회 엔드포인트 ──────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<CreateCompetitionResponse>> createCompetition(@RequestBody @Valid CreateCompetitionRequest request) {
        CreateCompetitionCommand command = new CreateCompetitionCommand(
                request.name(),
                request.type(),
                request.description(),
                request.firstSeed(),
                request.registerStartAt(),
                request.registerEndAt(),
                request.competitionStartAt(),
                request.competitionEndAt(),
                request.minParticipants(),
                request.maxParticipants()
        );
        Competition competition = competitionService.create(command);
        return ApiResponse.created("대회가 생성되었습니다.", CreateCompetitionResponse.from(competition));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FindCompetitionResponse>> findCompetitionById(@PathVariable UUID id) {
        Competition competition = competitionService.findById(id);
        return ApiResponse.ok(FindCompetitionResponse.from(competition));
    }

    @PostMapping("/{id}/publications")
    public ResponseEntity<ApiResponse<FindCompetitionResponse>> openCompetition(@PathVariable UUID id) {
        Competition competition = competitionService.openCompetition(id);
        return ApiResponse.ok("대회가 공개되었습니다.", FindCompetitionResponse.from(competition));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<FindCompetitionResponse>> updateCompetitionInfo(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateCompetitionRequest request) {
        UpdateCompetitionCommand command = new UpdateCompetitionCommand(
                id,
                request.name(),
                request.description(),
                request.registerStartAt(),
                request.registerEndAt(),
                request.competitionStartAt(),
                request.competitionEndAt(),
                request.minParticipants(),
                request.maxParticipants(),
                request.beforeContents(),
                request.afterContents(),
                request.reason(),
                request.updatedBy()
        );
        Competition competition = competitionService.updateInfo(command);
        return ApiResponse.ok(FindCompetitionResponse.from(competition));
    }

    @PostMapping("/{id}/starts")
    public ResponseEntity<ApiResponse<FindCompetitionResponse>> startCompetition(@PathVariable UUID id) {
        Competition competition = competitionService.startCompetition(id);
        return ApiResponse.ok("대회가 시작되었습니다.", FindCompetitionResponse.from(competition));
    }

    @PostMapping("/{id}/finishes")
    public ResponseEntity<ApiResponse<FindCompetitionResponse>> finishCompetition(@PathVariable UUID id) {
        Competition competition = competitionService.finishCompetition(id);
        return ApiResponse.ok("대회가 종료되었습니다.", FindCompetitionResponse.from(competition));
    }

    @PostMapping("/{id}/cancellations")
    public ResponseEntity<ApiResponse<FindCompetitionResponse>> cancelCompetition(@PathVariable UUID id) {
        Competition competition = competitionService.cancelCompetition(id);
        return ApiResponse.ok("대회가 취소되었습니다.", FindCompetitionResponse.from(competition));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<FindCompetitionResponse>> deleteCompetition(
            @PathVariable UUID id,
            @RequestParam String deletedBy) {
        return ApiResponse.ok("대회가 삭제되었습니다.", FindCompetitionResponse.from(competitionService.deleteCompetition(id, deletedBy)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<FindCompetitionResponse>>> findAllCompetition(
            @RequestParam(required = false) CompetitionStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Competition> competitions;
        if (status != null) {
            competitions = competitionService.findAllByStatus(status, pageable);
        } else {
            competitions = competitionService.findAll(pageable);
        }
        return ApiResponse.ok(competitions.map(FindCompetitionResponse::from));
    }

    // ─── 대회 변경 공지 엔드포인트 ────────────────────────────────────────────

    @GetMapping("/{id}/change-notices")
    public ResponseEntity<ApiResponse<List<FindCompetitionChangeNoticeResponse>>> findAllCompetitionChangeNotice(@PathVariable UUID id) {
        return ApiResponse.ok(competitionService.findChangeNotices(id)
                .stream()
                .map(FindCompetitionChangeNoticeResponse::from)
                .toList());
    }

    // ─── 대회 참여자 엔드포인트 ───────────────────────────────────────────────

    // 대회 신청 ( 대회가 조회 가능해야하고, 대회 신청기간에 신청 가능 )
    @PostMapping("/{competitionId}/participants")
    public ResponseEntity<ApiResponse<FindCompetitionParticipantResponse>> registerCompetition(
            @PathVariable UUID competitionId,
            @RequestBody @Valid JoinCompetitionRequest request
    ) {
        return ApiResponse.created("대회 신청이 완료되었습니다.",
                FindCompetitionParticipantResponse.from(
                        competitionParticipantService.registerCompetition(
                                new JoinCompetitionCommand(competitionId, request.userId(), request.nickname()))));
    }

    // 대회 신청 취소 ( 대회 신청 기간에 취소 가능 )
    @DeleteMapping("/{competitionId}/participants")
    public ResponseEntity<ApiResponse<FindCompetitionParticipantResponse>> cancelRegistration(
            @PathVariable UUID competitionId,
            @RequestBody @Valid JoinCompetitionRequest request
    ) {
        return ApiResponse.ok("대회 신청이 취소되었습니다.",
                FindCompetitionParticipantResponse.from(
                        competitionParticipantService.cancelRegistration(
                                new JoinCompetitionCommand(competitionId, request.userId(), request.nickname()))));
    }

    @GetMapping("/{competitionId}/participants")
    public ResponseEntity<ApiResponse<List<FindCompetitionParticipantResponse>>> findCompetitionParticipants(
            @PathVariable UUID competitionId
    ) {
        return ApiResponse.ok(competitionParticipantService.findAllByCompetitionId(competitionId)
                .stream()
                .map(FindCompetitionParticipantResponse::from)
                .toList());
    }
}
