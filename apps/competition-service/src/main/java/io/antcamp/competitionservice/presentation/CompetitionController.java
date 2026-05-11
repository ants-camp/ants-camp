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
import io.antcamp.competitionservice.presentation.dto.response.CreateCompetitionResponse;
import io.antcamp.competitionservice.presentation.dto.response.FindCompetitionChangeNoticeResponse;
import io.antcamp.competitionservice.presentation.dto.response.FindCompetitionParticipantResponse;
import io.antcamp.competitionservice.presentation.dto.response.FindCompetitionResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import io.antcamp.competitionservice.presentation.docs.CompetitionControllerDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/competitions")
public class CompetitionController implements CompetitionControllerDocs {

    private final CompetitionService competitionService;
    private final CompetitionParticipantService competitionParticipantService;

    // ─── 대회 엔드포인트 ──────────────────────────────────────────────────────

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateCompetitionResponse createCompetition(@RequestBody @Valid CreateCompetitionRequest request) {
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
        return CreateCompetitionResponse.from(competition);
    }

    @GetMapping("/{id}")
    public FindCompetitionResponse findCompetitionById(@PathVariable UUID id) {
        Competition competition = competitionService.findById(id);
        return FindCompetitionResponse.from(competition);
    }

    @PostMapping("/{id}/publications")
    public FindCompetitionResponse openCompetition(@PathVariable UUID id) {
        Competition competition = competitionService.openCompetition(id);
        return FindCompetitionResponse.from(competition);
    }

    @PatchMapping("/{id}")
    public FindCompetitionResponse updateCompetitionInfo(
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
        return FindCompetitionResponse.from(competition);
    }

    @PostMapping("/{id}/starts")
    public FindCompetitionResponse startCompetition(@PathVariable UUID id) {
        Competition competition = competitionService.startCompetition(id);
        return FindCompetitionResponse.from(competition);
    }

    @PostMapping("/{id}/finishes")
    public FindCompetitionResponse finishCompetition(@PathVariable UUID id) {
        Competition competition = competitionService.finishCompetition(id);
        return FindCompetitionResponse.from(competition);
    }

    @PostMapping("/{id}/cancellations")
    public FindCompetitionResponse cancelCompetition(@PathVariable UUID id) {
        Competition competition = competitionService.cancelCompetition(id);
        return FindCompetitionResponse.from(competition);
    }

    @DeleteMapping("/{id}")
    public FindCompetitionResponse deleteCompetition(
            @PathVariable UUID id,
            @RequestParam String deletedBy) {
        return FindCompetitionResponse.from(competitionService.deleteCompetition(id, deletedBy));
    }

    @GetMapping
    public Page<FindCompetitionResponse> findAllCompetition(
            @RequestParam(required = false) CompetitionStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Competition> competitions;
        if (status != null) {
            competitions = competitionService.findAllByStatus(status, pageable);
        } else {
            competitions = competitionService.findAll(pageable);
        }
        return competitions.map(FindCompetitionResponse::from);
    }

    // ─── 대회 변경 공지 엔드포인트 ────────────────────────────────────────────

    @GetMapping("/{id}/change-notices")
    public List<FindCompetitionChangeNoticeResponse> findAllCompetitionChangeNotice(@PathVariable UUID id) {
        return competitionService.findChangeNotices(id)
                .stream()
                .map(FindCompetitionChangeNoticeResponse::from)
                .toList();
    }

    // ─── 대회 참여자 엔드포인트 ───────────────────────────────────────────────

    // 대회 신청 ( 대회가 조회 가능해야하고, 대회 신청기간에 신청 가능 )
    @PostMapping("/{competitionId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    public FindCompetitionParticipantResponse registerCompetition(
            @PathVariable UUID competitionId,
            @RequestBody @Valid JoinCompetitionRequest request
    ) {
        return FindCompetitionParticipantResponse.from(
                competitionParticipantService.registerCompetition(
                        new JoinCompetitionCommand(competitionId, request.userId(), request.nickname())));
    }

    // 대회 신청 취소 ( 대회 신청 기간에 취소 가능 )
    @DeleteMapping("/{competitionId}/participants")
    public FindCompetitionParticipantResponse cancelRegistration(
            @PathVariable UUID competitionId,
            @RequestBody @Valid JoinCompetitionRequest request
    ) {
        return FindCompetitionParticipantResponse.from(
                competitionParticipantService.cancelRegistration(
                        new JoinCompetitionCommand(competitionId, request.userId(), request.nickname())));
    }

    @GetMapping("/{competitionId}/participants")
    public List<FindCompetitionParticipantResponse> findCompetitionParticipants(
            @PathVariable UUID competitionId
    ) {
        return competitionParticipantService.findAllByCompetitionId(competitionId)
                .stream()
                .map(FindCompetitionParticipantResponse::from)
                .toList();
    }
}
