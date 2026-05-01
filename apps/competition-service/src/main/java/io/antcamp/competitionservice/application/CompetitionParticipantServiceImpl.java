package io.antcamp.competitionservice.application;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.competitionservice.application.dto.JoinCompetitionCommand;
import io.antcamp.competitionservice.application.event.CompetitionEventProducer;
import io.antcamp.competitionservice.domain.event.CompetitionCancelledEvent;
import io.antcamp.competitionservice.domain.event.CompetitionRegisteredEvent;
import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.CompetitionParticipant;
import io.antcamp.competitionservice.domain.repository.CompetitionParticipantRepository;
import io.antcamp.competitionservice.domain.repository.CompetitionRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompetitionParticipantServiceImpl implements CompetitionParticipantService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final CompetitionEventProducer competitionEventProducer;

    @Transactional
    public void competitionRegister(JoinCompetitionCommand command) {
        // 1. 대회 조회 (Competition 비관적 락 먼저 획득 - 같은 대회 신청 요청을 직렬화)
        Competition competition = competitionRepository.findByIdForUpdate(command.competitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        // 2. 락 획득 후 중복 신청 체크 (이 시점엔 앞선 트랜잭션이 이미 commit된 상태)
        competitionParticipantRepository.findByUserIdAndCompetitionId(command.userId(), command.competitionId())
                .ifPresent(p -> {
                    throw new BusinessException(ErrorCode.INVALID_INPUT);
                });

        competition.register();
        competitionRepository.save(competition);

        // 3. 대회 참여자 저장
        CompetitionParticipant participant = CompetitionParticipant.create(
                command.userId(),
                command.nickname(),
                command.competitionId()
        );
        competitionParticipantRepository.save(participant);

        // 4. 대회 신청 이벤트 발행 (자산 서비스가 컨슘 → 해당 유저의 대회 전용 계좌 생성)
        CompetitionRegisteredEvent event = new CompetitionRegisteredEvent(
                competition.getCompetitionId(),
                competition.getName(),
                competition.getType().name(),
                competition.getFirstSeed(),
                command.userId()
        );
        competitionEventProducer.publishCompetitionRegistered(event);
    }

    @Transactional
    public void competitionCancel(JoinCompetitionCommand command) {
        // 1. 참여자 조회 (비관적 락)
        CompetitionParticipant participant = competitionParticipantRepository
                .findByUserIdAndCompetitionId(command.userId(), command.competitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        // 2. 대회 조회 (Competition 비관적 락 - 참가자 수 동시성 제어)
        Competition competition = competitionRepository.findByIdForUpdate(command.competitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        competition.cancelRegister();
        competitionRepository.save(competition);

        // 3. 참여자 소프트 삭제 (deletedBy는 임시로 userId 사용 - 추후 인증 연동 시 교체)
        competitionParticipantRepository.delete(participant, command.userId().toString());

        // 4. 대회 취소 이벤트 발행 (자산 서비스가 컨슘 → 해당 유저의 대회 전용 계좌 정리)
        CompetitionCancelledEvent event = new CompetitionCancelledEvent(
                command.competitionId(),
                command.userId()
        );
        competitionEventProducer.publishCompetitionCancelled(event);
    }

    @Transactional(readOnly = true)
    public List<CompetitionParticipant> findAllByCompetitionId(UUID competitionId) {
        return competitionParticipantRepository.findAllByCompetitionId(competitionId);
    }
}
