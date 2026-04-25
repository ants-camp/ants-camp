package io.antcamp.competitionservice.application;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.competitionservice.application.dto.JoinCompetitionCommand;
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

    @Transactional
    public void join(JoinCompetitionCommand command) {
        // 중복 신청 체크 (비관적 락)
        joinHistoryRepository.findByUserIdAndCompetitionId(command.userId(), command.competitionId())
                .ifPresent(h -> {
                    throw new BusinessException(ErrorCode.INVALID_INPUT);
                });

        // 대회 상태 검증 + 참가자 수 증가
        Competition competition = competitionRepository.findById(command.competitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        competition.register();
        competitionRepository.save(competition);

        // 신청 이력 저장
        JoinHistory joinHistory = JoinHistory.createJoinHistory(
                command.userId(),
                command.nickname(),
                command.competitionId()
        );
        joinHistoryRepository.save(joinHistory);
    }

    @Transactional
    public void cancel(JoinCompetitionCommand command) {
        // 신청 이력 조회 (비관적 락)
        JoinHistory joinHistory = joinHistoryRepository
                .findByUserIdAndCompetitionId(command.userId(), command.competitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        // 대회 상태 검증 + 참가자 수 감소
        Competition competition = competitionRepository.findById(command.competitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        competition.cancelRegister();
        competitionRepository.save(competition);

        // 신청 이력 소프트 삭제
        // deletedBy는 임시로 userId 사용 (추후 인증 연동 시 교체)
        joinHistoryRepository.delete(joinHistory, command.userId().toString());
    }
}
