package io.antcamp.rankingservice.application;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.rankingservice.application.dto.RankingResult;
import io.antcamp.rankingservice.domain.event.RankingUpdateRequestedEvent;
import io.antcamp.rankingservice.domain.event.TotalAssetCalcuatedEvent;
import io.antcamp.rankingservice.domain.model.RankTier;
import io.antcamp.rankingservice.domain.model.Ranking;
import io.antcamp.rankingservice.domain.repository.RankingRedisRepository;
import io.antcamp.rankingservice.domain.repository.RankingRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final RankingRepository rankingRepository;
    private final RankingRedisRepository rankingRedisRepository;

    @Override
    public void upsertRanking(UUID competitionId, UUID userId, Double totalAsset) {
        rankingRedisRepository.upsertScore(competitionId, userId, totalAsset);
    }

    @Override
    public void batchUpsertRankings(UUID competitionId, List<RankingUpdateRequestedEvent.ParticipantAsset> participants) {
        // 1분마다 호출 — 참가자 전체 총자산을 Redis에 일괄 갱신 (DB 저장 없음)
        // 매매 미체결 상태에서 보유주식 시가 변동을 랭킹에 반영하기 위함
        participants.forEach(p ->
                rankingRedisRepository.upsertScore(competitionId, p.userId(), p.totalAsset())
        );
    }

    @Override
    public List<RankingResult> getTopRankings(UUID competitionId, int page, int size) {
        long offset = (long) page * size;
        return rankingRedisRepository.getTopRankings(competitionId, offset, size)
                .stream()
                .map(entry -> new RankingResult(
                        entry.userId(),
                        entry.totalAsset(),
                        entry.rank()))
                .toList();
    }

    @Override
    public RankingResult getMyRanking(UUID competitionId, UUID userId) {
        // getRank(), getScore() 모두 userId 기반으로 직접 조회하므로
        // 다른 참가자의 순위 변경과 무관하게 항상 정확한 값을 반환한다.
        long rank0based = rankingRedisRepository.getRank(competitionId, userId);
        if (rank0based < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        Double totalAsset = rankingRedisRepository.getScore(competitionId, userId);
        if (totalAsset == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        long rank1based = rank0based + 1;
        return new RankingResult(userId, totalAsset, rank1based);
    }

    @Override
    @Transactional
    public void finalizeRankings(UUID competitionId) {
        long total = rankingRedisRepository.getTotalCount(competitionId);
        if (total == 0) {
            return;
        }

        List<RankingRedisRepository.RankingEntry> all =
                rankingRedisRepository.getTopRankings(competitionId, 0, total);

        all.forEach(entry -> {
            Ranking ranking = Ranking.createRanking(competitionId, entry.userId());
            ranking.finalize(RankTier.from(entry.rank(), total));
            rankingRepository.save(ranking);
        });
    }

    @Override
    @Transactional
    public void finalizeRankingsWithValuations(
            UUID competitionId,
            List<TotalAssetCalcuatedEvent.ParticipantTotalAsset> valuations
    ) {
        // 1. 최종 총자산을 Redis에 upsert (자산 서비스가 계산한 최종값으로 덮어쓰기)
        valuations.forEach(v ->
                rankingRedisRepository.upsertScore(competitionId, v.userId(), v.totalAsset())
        );

        // 2. Redis 기반으로 최종 순위 계산 후 DB에 저장
        finalizeRankings(competitionId);
    }
}
