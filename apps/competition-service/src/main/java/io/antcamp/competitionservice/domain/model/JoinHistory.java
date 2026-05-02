package io.antcamp.competitionservice.domain.model;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class JoinHistory {

    private final UUID joinHistoryId;
    private final UUID userId;
    private final String nickname;
    private final UUID competitionId;

    @Builder(access = AccessLevel.PRIVATE)
    private JoinHistory(
            UUID joinHistoryId,
            UUID userId,
            String nickname,
            UUID competitionId
    ) {
        this.joinHistoryId = joinHistoryId;
        this.userId = userId;
        this.nickname = nickname;
        this.competitionId = competitionId;
        validate();
    }

    // ─── 정적 팩토리 메서드 ───────────────────────────────────────────────

    public static JoinHistory createJoinHistory(UUID userId, String nickname, UUID competitionId) {
        return JoinHistory.builder()
                .joinHistoryId(UUID.randomUUID())
                .userId(userId)
                .nickname(nickname)
                .competitionId(competitionId)
                .build();
    }

    public static JoinHistory from(
            UUID joinHistoryId,
            UUID userId,
            String nickname,
            UUID competitionId
    ) {
        return JoinHistory.builder()
                .joinHistoryId(joinHistoryId)
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
        if (joinHistoryId == null) {
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
