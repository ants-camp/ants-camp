package io.antcamp.rankingservice.application;

import io.antcamp.rankingservice.application.dto.RankingResult;
import io.antcamp.rankingservice.domain.event.TotalAssetCalcuatedEvent;
import java.util.List;
import java.util.UUID;

public interface RankingService {
    // 매매 체결 시 단건 Redis 갱신 (실시간 순위, DB 저장 없음)
    void upsertRanking(UUID competitionId, UUID userId, Double totalAsset);

//    // 1분마다 대회 참가자 전체 총자산을 Redis에 일괄 갱신 (DB 저장 없음)
//    void batchUpsertRankings(UUID competitionId, List<RankingUpdateRequestedEvent.ParticipantAsset> participants);

    // 대회 전체 랭킹 조회 (진행 중)
    List<RankingResult> getTopRankings(UUID competitionId, int page, int size);

    // 내 순위 조회 (진행 중)
    RankingResult getMyRanking(UUID competitionId, UUID userId);

    // 대회 종료 시 최종 순위 확정 (HTTP - 수동 트리거용)
    void finalizeRankings(UUID competitionId);

    // 대회 종료 이벤트 수신 시 최종 순위 확정 (Kafka - 자산 서비스로부터 최종 총자산 수신)
    // 1) 최종 총자산을 Redis에 upsert
    // 2) Redis 기반으로 최종 순위 계산 후 DB에 저장
    void finalizeRankingsWithValuations(UUID competitionId,
                                        List<TotalAssetCalcuatedEvent.ParticipantTotalAsset> valuations);
}
