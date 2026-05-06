package io.antcamp.competitionservice.presentation.dto;

import io.antcamp.competitionservice.domain.model.CompetitionParticipant;
import java.util.UUID;

/**
 * 대회 참여자 조회 응답 DTO.
 * 매매 서비스에서 FeignClient로 호출하여 대회 참여자 목록을 수신한다.
 * 매매 서비스는 이 목록을 기반으로 각 유저의 총자산을 계산하여
 * RankingUpdateRequestedEvent를 발행한다.
 */
public record FindCompetitionParticipantResponse(
        UUID participantId,
        UUID userId,
        String nickname,
        UUID competitionId
) {
    public static FindCompetitionParticipantResponse from(CompetitionParticipant participant) {
        return new FindCompetitionParticipantResponse(
                participant.getParticipantId(),
                participant.getUserId(),
                participant.getNickname(),
                participant.getCompetitionId()
        );
    }
}
