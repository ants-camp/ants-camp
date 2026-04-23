package io.antcamp.competitionservice.domain;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

/**
 * 대회 참여자 명부 도메인 클래스 (순수 도메인 모델 - JPA 의존성 없음)
 */
@Getter
public class JoinHistory {

    private final UUID participantId;
    private final UUID userId;
    private final String nickname;
    private final UUID competitionId;

    @Builder(builderMethodName = "createBuilder", access = AccessLevel.PRIVATE)
    private JoinHistory(UUID userId, String nickname, UUID competitionId) {
        this.participantId = UUID.randomUUID();
        this.userId = userId;
        this.nickname = nickname;
        this.competitionId = competitionId;
    }

    @Builder(builderMethodName = "fromBuilder", access = AccessLevel.PRIVATE)
    private JoinHistory(UUID participantId, UUID userId, String nickname, UUID competitionId) {
        this.participantId = participantId;
        this.userId = userId;
        this.nickname = nickname;
        this.competitionId = competitionId;
    }

    // ─── 정적 팩토리 메서드 ───────────────────────────────────────────────

    public static JoinHistory createJoinHistory(UUID userId, String nickname, UUID competitionId) {
        validate(userId, nickname, competitionId);

        return JoinHistory.createBuilder()
                .userId(userId)
                .nickname(nickname)
                .competitionId(competitionId)
                .build();
    }

    public static JoinHistory from(UUID participantId, UUID userId, String nickname, UUID competitionId) {
        return JoinHistory.fromBuilder()
                .participantId(participantId)
                .userId(userId)
                .nickname(nickname)
                .competitionId(competitionId)
                .build();
    }

    // ─── 도메인 행위 ─────────────────────────────────────────────────────

    // 같은 대회에 중복 참여하는것을 방지할 떄 사용, 서비스에서 이 값이 true면 예외 반환하도록 작성하자.
    public boolean isSameCompetition(UUID competitionId) {
        return this.competitionId.equals(competitionId);
    }

    private static void validate(UUID userId, String nickname, UUID competitionId) {
        if (userId == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }
        if (competitionId == null) {
            throw new IllegalArgumentException("대회 ID는 필수입니다.");
        }
    }
}
