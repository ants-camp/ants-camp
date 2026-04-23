package io.antcamp.competitionservice.presentation;

import io.antcamp.competitionservice.application.CompetitionService;
import io.antcamp.competitionservice.application.dto.CreateCompetitionCommand;
import io.antcamp.competitionservice.domain.Competition;
import io.antcamp.competitionservice.presentation.dto.CreateCompetitionRequest;
import io.antcamp.competitionservice.presentation.dto.CreateCompetitionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
        Competition competition = competitionService.create(CreateCompetitionCommand.from(request));
        return CreateCompetitionResponse.from(competition);
    }
}
