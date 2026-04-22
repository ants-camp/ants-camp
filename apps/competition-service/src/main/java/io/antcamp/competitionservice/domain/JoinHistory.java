package io.antcamp.competitionservice.domain;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * 대회 참여자 명부 도메인 클래스 (순수 도메인 모델 - JPA 의존성 없음)
 */
@Getter
public class JoinHistory {

    private final UUID participantId;
    private final UUID userId;       // 유저 서버의 PK값 (외부 서비스)
    private final String nickname;   // 참가 시점의 닉네임 스냅샷
    private final UUID competitionId;

    // 신규 참가 등록 - participantId는 도메인이 직접 결정
    @Builder(builderMethodName = "create")
    public JoinHistory(UUID userId, String nickname, UUID competitionId) {
        this.participantId = UUID.randomUUID();
        this.userId = userId;
        this.nickname = nickname;
        this.competitionId = competitionId;
    }

    // 재구성용 생성자 (toDomain() 전용)
    @Builder(builderMethodName = "restore")
    public JoinHistory(UUID participantId, UUID userId, String nickname, UUID competitionId) {
        this.participantId = participantId;
        this.userId = userId;
        this.nickname = nickname;
        this.competitionId = competitionId;
    }

    // ─── 도메인 행위 ──────────────────────────────────────────────────────

    /**
     * 동일 대회 중복 참가 여부 확인
     */
    public boolean isSameCompetition(UUID competitionId) {
        return this.competitionId.equals(competitionId);
    }
}
