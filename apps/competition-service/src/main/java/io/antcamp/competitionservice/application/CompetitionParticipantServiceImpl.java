package io.antcamp.competitionservice.application;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.competitionservice.application.dto.CancelCompetitionCommand;
import io.antcamp.competitionservice.application.dto.JoinCompetitionCommand;
import io.antcamp.competitionservice.domain.event.CompetitionCancelledEvent;
import io.antcamp.competitionservice.domain.event.CompetitionRegisteredEvent;
import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.CompetitionParticipant;
import io.antcamp.competitionservice.domain.repository.CompetitionParticipantRepository;
import io.antcamp.competitionservice.domain.repository.CompetitionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompetitionParticipantServiceImpl implements CompetitionParticipantService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public CompetitionParticipant registerCompetition(JoinCompetitionCommand command) {
        // 1. 대회 조회 (Competition 비관적 락 먼저 획득 - 같은 대회 신청 요청을 직렬화)
        Competition competition = competitionRepository.findByIdWithLock(command.competitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_NOT_FOUND));

        if (competition.getRegisterPeriod().getEndAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.COMPETITION_CANNOT_REGISTER);
        }

        // 락 획득 후 중복 신청 체크 (이 시점엔 앞선 트랜잭션이 이미 commit된 상태)
        competitionParticipantRepository.findByUserIdAndCompetitionId(command.userId(), command.competitionId())
                .ifPresent(p -> {
                    throw new BusinessException(ErrorCode.COMPETITION_ALREADY_REGISTERED);
                });

        competition.register();
        competitionRepository.save(competition);

        CompetitionParticipant participant = CompetitionParticipant.create(
                command.userId(),
                command.username(),
                command.competitionId()
        );
        CompetitionParticipant saved = competitionParticipantRepository.save(participant);

        // DB 커밋 완료 후 리스너가 Kafka로 전달
        applicationEventPublisher.publishEvent(new CompetitionRegisteredEvent(
                competition.getCompetitionId(),
                competition.getName(),
                competition.getType().name(),
                competition.getFirstSeed(),
                command.userId()
        ));

        return saved;
    }

    @Transactional
    public CompetitionParticipant cancelRegistration(CancelCompetitionCommand command) {
        // 1. 대회 조회 (Competition 비관적 락 - 참가자 수 동시성 제어)
        Competition competition = competitionRepository.findByIdWithLock(command.competitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_NOT_FOUND));
        competition.cancelRegister();
        competitionRepository.save(competition);

        CompetitionParticipant participant = competitionParticipantRepository
                .findByUserIdAndCompetitionId(command.userId(), command.competitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_PARTICIPANT_NOT_FOUND));

        competitionParticipantRepository.delete(participant, command.userId().toString());

        // DB 커밋 완료 후 리스너가 Kafka로 전달
        applicationEventPublisher.publishEvent(new CompetitionCancelledEvent(
                command.competitionId(),
                command.userId()
        ));

        return participant;
    }

    @Transactional(readOnly = true)
    public List<CompetitionParticipant> findAllByCompetitionId(UUID competitionId) {
        return competitionParticipantRepository.findAllByCompetitionId(competitionId);
    }
}
