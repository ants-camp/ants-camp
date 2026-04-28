package io.antcamp.rankingservice.application;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.rankingservice.application.dto.RankingResult;
import io.antcamp.rankingservice.domain.event.ParticipantsValuatedPayload;
import io.antcamp.rankingservice.domain.model.RankTier;
import io.antcamp.rankingservice.domain.model.Ranking;
import io.antcamp.rankingservice.domain.repository.RankingRedisRepository;
import io.antcamp.rankingservice.domain.repository.RankingRepository;
import java.math.BigDecimal;
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
    public void upsertRanking(UUID competitionId, UUID userId, BigDecimal totalAsset) {
        rankingRedisRepository.upsertScore(competitionId, userId, totalAsset);
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
        long rank0based = rankingRedisRepository.getRank(competitionId, userId);
        if (rank0based < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return rankingRedisRepository.getTopRankings(competitionId, rank0based, 1)
                .stream()
                .filter(e -> e.userId().equals(userId))
                .map(e -> new RankingResult(e.userId(), e.totalAsset(), e.rank()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
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
            List<ParticipantsValuatedPayload.ParticipantValuation> valuations
    ) {
        // 1. 최종 총자산을 Redis에 upsert (자산 서비스가 계산한 최종값으로 덮어쓰기)
        valuations.forEach(v ->
                rankingRedisRepository.upsertScore(
                        competitionId,
                        v.userId(),
                        java.math.BigDecimal.valueOf(v.totalAsset())
                )
        );

        // 2. Redis 기반으로 최종 순위 계산 후 DB에 저장
        finalizeRankings(competitionId);
    }
}
