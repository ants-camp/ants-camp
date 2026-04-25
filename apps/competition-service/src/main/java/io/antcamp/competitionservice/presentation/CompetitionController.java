package io.antcamp.competitionservice.presentation;

import io.antcamp.competitionservice.application.CompetitionService;
import io.antcamp.competitionservice.application.dto.CreateCompetitionCommand;
import io.antcamp.competitionservice.application.dto.UpdateCompetitionCommand;
import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import io.antcamp.competitionservice.presentation.dto.CreateCompetitionRequest;
import io.antcamp.competitionservice.presentation.dto.CreateCompetitionResponse;
import io.antcamp.competitionservice.presentation.dto.FindCompetitionChangeNoticeResponse;
import io.antcamp.competitionservice.presentation.dto.FindCompetitionResponse;
import io.antcamp.competitionservice.presentation.dto.UpdateCompetitionRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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
public class CompetitionController {

    private final CompetitionService competitionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateCompetitionResponse create(@RequestBody @Valid CreateCompetitionRequest request) {
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
    public FindCompetitionResponse findById(@PathVariable UUID id) {
        Competition competition = competitionService.findById(id);
        return FindCompetitionResponse.from(competition);
    }

    @GetMapping
    public Page<FindCompetitionResponse> findAll(
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

    @PatchMapping("/{id}/publish")
    public FindCompetitionResponse publish(@PathVariable UUID id) {
        Competition competition = competitionService.publish(id);
        return FindCompetitionResponse.from(competition);
    }

    @PatchMapping("/{id}")
    public FindCompetitionResponse updateInfo(
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

    @PatchMapping("/{id}/cancel")
    public FindCompetitionResponse cancel(@PathVariable UUID id) {
        Competition competition = competitionService.cancel(id);
        return FindCompetitionResponse.from(competition);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id,
            @RequestParam String deletedBy) {
        competitionService.delete(id, deletedBy);
    }

    @GetMapping("/{id}/change-notices")
    public List<FindCompetitionChangeNoticeResponse> findChangeNotices(@PathVariable UUID id) {
        return competitionService.findChangeNotices(id)
                .stream()
                .map(FindCompetitionChangeNoticeResponse::from)
                .toList();
    }
}
