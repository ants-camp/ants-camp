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
        Competition competition = Competition.create()
                .name(command.name())
                .type(command.type())
                .description(command.description())
                .registerPeriod(new RegisterPeriod(command.registerStartAt(), command.registerEndAt()))
                .competitionPeriod(new CompetitionPeriod(command.competitionStartAt(), command.competitionEndAt()))
                .participantCount(new ParticipantCount(command.minParticipants(), command.maxParticipants(), 0))
                .build();

        return competitionRepository.save(competition);
    }
}
