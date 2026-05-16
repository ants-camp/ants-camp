package io.antcamp.rankingservice.application;

import io.antcamp.rankingservice.application.dto.CompetitionHistoryResult;
import io.antcamp.rankingservice.application.dto.RankingResult;
import io.antcamp.rankingservice.domain.event.TotalAssetCalculatedEvent;
import java.util.List;
import java.util.UUID;

public interface RankingService {

    // Update

    /**
     * 대회 종료 시 최종 순위 확정 (HTTP - 수동 트리거용)
     *
     * @return 확정된 랭킹 수
     */
    int finalizeRankings(UUID competitionId);

    /**
     * 대회 종료 이벤트 수신 시 최종 순위 확정 (Kafka - 자산 서비스로부터 최종 총자산 수신) 1) valuations를 자산 내림차순 정렬 → DB에 직접 저장 (Redis 거치지 않음) 2) DB
     * 커밋 완료 후 Redis를 최종값으로 동기화
     */
    void finalizeRankingsWithValuations(UUID competitionId,
                                        List<TotalAssetCalculatedEvent.ParticipantTotalAsset> valuations);

    // Search
    List<RankingResult> findTopRankings(UUID competitionId, int page, int size);

    // Read
    RankingResult findMyRanking(UUID competitionId, UUID userId);

    /**
     * 유저의 전체 대회 참여 이력 조회 (확정된 것만, DB 기반)
     */
    List<CompetitionHistoryResult> findMyHistory(UUID userId);
}
