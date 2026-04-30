package io.antcamp.competitionservice.domain.model;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CompetitionParticipant {

    private final UUID participantId;
    private final UUID userId;
    private final String nickname;
    private final UUID competitionId;

    @Builder(access = AccessLevel.PRIVATE)
    private CompetitionParticipant(
            UUID participantId,
            UUID userId,
            String nickname,
            UUID competitionId
    ) {
        this.participantId = participantId;
        this.userId = userId;
        this.nickname = nickname;
        this.competitionId = competitionId;
        validate();
    }

    // ─── 정적 팩토리 메서드 ───────────────────────────────────────────────

    public static CompetitionParticipant create(UUID userId, String nickname, UUID competitionId) {
        return CompetitionParticipant.builder()
                .participantId(UUID.randomUUID())
                .userId(userId)
                .nickname(nickname)
                .competitionId(competitionId)
                .build();
    }

    public static CompetitionParticipant from(
            UUID participantId,
            UUID userId,
            String nickname,
            UUID competitionId
    ) {
        return CompetitionParticipant.builder()
                .participantId(participantId)
                .userId(userId)
                .nickname(nickname)
                .competitionId(competitionId)
                .build();
    }

    // ─── 도메인 행위 ─────────────────────────────────────────────────────

    public boolean isSameCompetition(UUID competitionId) {
        return this.competitionId.equals(competitionId);
    }

    private void validate() {
        if (participantId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (nickname == null || nickname.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (competitionId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
