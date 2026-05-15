package io.antcamp.competitionservice.presentation;

import common.dto.CommonResponse;
import io.antcamp.competitionservice.application.CompetitionParticipantService;
import io.antcamp.competitionservice.application.CompetitionService;
import io.antcamp.competitionservice.application.dto.CancelCompetitionCommand;
import io.antcamp.competitionservice.application.dto.CreateCompetitionCommand;
import io.antcamp.competitionservice.application.dto.JoinCompetitionCommand;
import io.antcamp.competitionservice.application.dto.UpdateCompetitionCommand;
import io.antcamp.competitionservice.application.event.CompetitionEventProducer;
import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import io.antcamp.competitionservice.infrastructure.scheduler.CompetitionTickScheduler;
import io.antcamp.competitionservice.presentation.docs.CompetitionControllerDocs;
import io.antcamp.competitionservice.presentation.dto.request.CreateCompetitionRequest;
import io.antcamp.competitionservice.presentation.dto.request.UpdateCompetitionRequest;
import io.antcamp.competitionservice.presentation.dto.response.CreateCompetitionResponse;
import io.antcamp.competitionservice.presentation.dto.response.FindCompetitionChangeNoticeResponse;
import io.antcamp.competitionservice.presentation.dto.response.FindCompetitionParticipantResponse;
import io.antcamp.competitionservice.presentation.dto.response.FindCompetitionResponse;
import jakarta.validation.Valid;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/competitions")
@Slf4j
public class CompetitionController implements CompetitionControllerDocs {

    private final CompetitionTickScheduler competitionTickScheduler;
    private final CompetitionEventProducer competitionEventProducer;
    private final CompetitionService competitionService;
    private final CompetitionParticipantService competitionParticipantService;

    // ─── 대회 엔드포인트 ──────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<CommonResponse<CreateCompetitionResponse>> createCompetition(
            @RequestBody @Valid CreateCompetitionRequest request) {
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
        log.info("컨트롤러 - 대회 저장 완료");
        return CommonResponse.created("대회가 생성되었습니다.", CreateCompetitionResponse.from(competition));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<FindCompetitionResponse>> findCompetitionById(@PathVariable UUID id) {
        Competition competition = competitionService.findById(id);
        return CommonResponse.ok(FindCompetitionResponse.from(competition));
    }

    @PostMapping("/{id}/publications")
    public ResponseEntity<CommonResponse<FindCompetitionResponse>> openCompetition(@PathVariable UUID id) {
        Competition competition = competitionService.openCompetition(id);
        return CommonResponse.ok("대회가 공개되었습니다.", FindCompetitionResponse.from(competition));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<FindCompetitionResponse>> updateCompetitionInfo(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID updatedBy,
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
                updatedBy.toString()
        );
        Competition competition = competitionService.updateInfo(command);
        return CommonResponse.ok(FindCompetitionResponse.from(competition));
    }

    @PostMapping("/{id}/starts")
    public ResponseEntity<CommonResponse<FindCompetitionResponse>> startCompetition(@PathVariable UUID id) {
        Competition competition = competitionService.startCompetition(id);
        return CommonResponse.ok("대회가 시작되었습니다.", FindCompetitionResponse.from(competition));
    }

    @PostMapping("/{id}/finishes")
    public ResponseEntity<CommonResponse<FindCompetitionResponse>> finishCompetition(@PathVariable UUID id) {
        Competition competition = competitionService.finishCompetition(id);
        return CommonResponse.ok("대회가 종료되었습니다.", FindCompetitionResponse.from(competition));
    }

    @PostMapping("/{id}/cancellations")
    public ResponseEntity<CommonResponse<FindCompetitionResponse>> cancelCompetition(@PathVariable UUID id) {
        Competition competition = competitionService.cancelCompetition(id);
        return CommonResponse.ok("대회가 취소되었습니다.", FindCompetitionResponse.from(competition));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<FindCompetitionResponse>> deleteCompetition(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID deletedBy) {
        return CommonResponse.ok("대회가 삭제되었습니다.",
                FindCompetitionResponse.from(competitionService.deleteCompetition(id, deletedBy.toString())));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<FindCompetitionResponse>>> findAllCompetition(
            @RequestParam(required = false) CompetitionStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Competition> competitions;
        if (status != null) {
            competitions = competitionService.findAllByStatus(status, pageable);
        } else {
            competitions = competitionService.findAll(pageable);
        }
        return CommonResponse.ok(competitions.map(FindCompetitionResponse::from));
    }

    // ─── 대회 변경 공지 엔드포인트 ────────────────────────────────────────────

    @GetMapping("/{id}/change-notices")
    public ResponseEntity<CommonResponse<List<FindCompetitionChangeNoticeResponse>>> findAllCompetitionChangeNotice(
            @PathVariable UUID id) {
        return CommonResponse.ok(competitionService.findChangeNotices(id)
                .stream()
                .map(FindCompetitionChangeNoticeResponse::from)
                .toList());
    }

    // ─── 대회 참여자 엔드포인트 ───────────────────────────────────────────────

    // 대회 신청 ( 대회가 조회 가능해야하고, 대회 신청기간에 신청 가능 )
    @PostMapping("/{competitionId}/participants")
    public ResponseEntity<CommonResponse<FindCompetitionParticipantResponse>> registerCompetition(
            @PathVariable UUID competitionId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Name") String encodedUsername
    ) {
        String username = URLDecoder.decode(encodedUsername, StandardCharsets.UTF_8);
        return CommonResponse.created("대회 신청이 완료되었습니다.",
                FindCompetitionParticipantResponse.from(
                        competitionParticipantService.registerCompetition(
                                new JoinCompetitionCommand(competitionId, userId, username))));
    }

    // 대회 신청 취소 ( 대회 신청 기간에 취소 가능 )
    @DeleteMapping("/{competitionId}/participants")
    public ResponseEntity<CommonResponse<FindCompetitionParticipantResponse>> cancelRegistration(
            @PathVariable UUID competitionId,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        return CommonResponse.ok("대회 신청이 취소되었습니다.",
                FindCompetitionParticipantResponse.from(
                        competitionParticipantService.cancelRegistration(
                                new CancelCompetitionCommand(competitionId, userId))));
    }

    // 특정 대회의 참가자 목록 조회
    @GetMapping("/{competitionId}/participants")
    public ResponseEntity<CommonResponse<List<FindCompetitionParticipantResponse>>> findCompetitionParticipants(
            @PathVariable UUID competitionId
    ) {
        return CommonResponse.ok(competitionParticipantService.findAllByCompetitionId(competitionId)
                .stream()
                .map(FindCompetitionParticipantResponse::from)
                .toList());
    }

    /**
     * 특정 대회 한 건에 대한 랭킹 갱신을 수동으로 트리거한다. 운영/테스트 용도로만 사용한다 — 평상시에는 1분 주기 스케줄러가 ONGOING 대회를 자동 tick 한다.
     */
    @PostMapping("/refresh")
    public void refreshCompetitionRanking() {
        competitionTickScheduler.publishCompetitionTicks();
    }
}
