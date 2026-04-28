package io.antcamp.competitionservice.application;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.competitionservice.application.dto.JoinCompetitionCommand;
import io.antcamp.competitionservice.application.event.CompetitionEventProducer;
import io.antcamp.competitionservice.domain.event.CompetitionRegisteredPayload;
import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.JoinHistory;
import io.antcamp.competitionservice.domain.repository.CompetitionRepository;
import io.antcamp.competitionservice.domain.repository.JoinHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JoinHistoryServiceImpl implements JoinHistoryService {

    private final CompetitionRepository competitionRepository;
    private final JoinHistoryRepository joinHistoryRepository;
    private final CompetitionEventProducer competitionEventProducer;

    @Transactional
    public void join(JoinCompetitionCommand command) {
        // 1. 중복 신청 체크 (JoinHistory 비관적 락 - 같은 사용자 동시 신청 방지)
        joinHistoryRepository.findByUserIdAndCompetitionId(command.userId(), command.competitionId())
                .ifPresent(h -> {
                    throw new BusinessException(ErrorCode.INVALID_INPUT);
                });

        // 2. 대회 조회 (Competition 비관적 락 - 참가자 수 동시성 제어) ← 변경
        Competition competition = competitionRepository.findByIdForUpdate(command.competitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        competition.register();
        competitionRepository.save(competition);

        // 3. 신청 이력 저장
        JoinHistory joinHistory = JoinHistory.createJoinHistory(
                command.userId(),
                command.nickname(),
                command.competitionId()
        );
        joinHistoryRepository.save(joinHistory);

        // 4. 대회 신청 이벤트 발행 (자산 서비스가 컨슘 → 해당 유저의 대회 전용 계좌 생성)
        CompetitionRegisteredPayload payload = new CompetitionRegisteredPayload(
                competition.getCompetitionId(),
                competition.getName(),
                competition.getType().name(),
                competition.getFirstSeed(),
                command.userId()
        );
        competitionEventProducer.publishCompetitionRegistered(payload);
    }

    @Transactional
    public void cancel(JoinCompetitionCommand command) {
        // 1. 신청 이력 조회 (JoinHistory 비관적 락)
        JoinHistory joinHistory = joinHistoryRepository
                .findByUserIdAndCompetitionId(command.userId(), command.competitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        // 2. 대회 조회 (Competition 비관적 락 - 참가자 수 동시성 제어) ← 변경
        Competition competition = competitionRepository.findByIdForUpdate(command.competitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        competition.cancelRegister();
        competitionRepository.save(competition);

        // 3. 신청 이력 소프트 삭제
        // deletedBy는 임시로 userId 사용 (추후 인증 연동 시 교체)
        joinHistoryRepository.delete(joinHistory, command.userId().toString());
    }
}
