package io.antcamp.competitionservice.application;

import io.antcamp.competitionservice.application.dto.CreateCompetitionCommand;
import io.antcamp.competitionservice.domain.Competition;
import io.antcamp.competitionservice.domain.repository.CompetitionRepository;
import io.antcamp.competitionservice.domain.vo.CompetitionPeriod;
import io.antcamp.competitionservice.domain.vo.ParticipantCount;
import io.antcamp.competitionservice.domain.vo.RegisterPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompetitionServiceImpl implements CompetitionService {

    private final CompetitionRepository competitionRepository;

    @Override
    @Transactional
    public Competition create(CreateCompetitionCommand command) {
        Competition competition = Competition.createCompetition(
                command.name(),
                command.type(),
                command.description(),
                command.firstSeed(),
                RegisterPeriod.of(command.registerStartAt(), command.registerEndAt()),
                CompetitionPeriod.of(command.competitionStartAt(), command.competitionEndAt()),
                ParticipantCount.of(command.minParticipants(), command.maxParticipants())
        );

        return competitionRepository.save(competition);
    }
}
