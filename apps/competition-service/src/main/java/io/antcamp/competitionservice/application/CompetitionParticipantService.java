package io.antcamp.competitionservice.application;

import io.antcamp.competitionservice.application.dto.JoinCompetitionCommand;
import io.antcamp.competitionservice.domain.model.CompetitionParticipant;
import java.util.List;
import java.util.UUID;

public interface CompetitionParticipantService {
    // 대회 신청
    void competitionRegister(JoinCompetitionCommand command);

    // 대회 취소
    void competitionCancel(JoinCompetitionCommand command);

    // 특정 대회의 모든 참가자 조회
    List<CompetitionParticipant> findAllByCompetitionId(UUID competitionId);
}
