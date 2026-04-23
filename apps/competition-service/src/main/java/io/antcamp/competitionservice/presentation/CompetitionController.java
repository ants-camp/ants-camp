package io.antcamp.competitionservice.presentation;

import io.antcamp.competitionservice.application.CompetitionService;
import io.antcamp.competitionservice.application.dto.CreateCompetitionCommand;
import io.antcamp.competitionservice.domain.Competition;
import io.antcamp.competitionservice.domain.CompetitionStatus;
import io.antcamp.competitionservice.presentation.dto.CreateCompetitionRequest;
import io.antcamp.competitionservice.presentation.dto.CreateCompetitionResponse;
import io.antcamp.competitionservice.presentation.dto.FindCompetitionListResponse;
import io.antcamp.competitionservice.presentation.dto.FindCompetitionResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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
    public List<FindCompetitionListResponse> findAll(
            @RequestParam(required = false) CompetitionStatus status) {
        List<Competition> competitions;
        if (status != null) {
            competitions = competitionService.findAllByStatus(status);
        } else {
            competitions = competitionService.findAll();
        }
        return competitions.stream()
                .map(FindCompetitionListResponse::from)
                .toList();
    }
}
