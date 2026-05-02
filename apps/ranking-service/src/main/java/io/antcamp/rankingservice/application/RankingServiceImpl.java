package io.antcamp.rankingservice.application;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.rankingservice.application.dto.RankingResult;
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

    // ── Update ────────────────────────────────────────────────────────────────

    // 매매 체결 시 랭킹 저장(갱신)
    @Override
    public void updateLiveRanking(UUID competitionId, UUID userId, Double totalAsset) {
        rankingRedisRepository.upsertScore(competitionId, userId, totalAsset);
    }

    // 대회 종료 시 최종 랭킹을 DB에 저장하는 메서드
    @Override
    @Transactional
    public void finalizeRankings(UUID competitionId) {
        long total = rankingRedisRepository.getTotalCount(competitionId);
        if (total == 0) {
            return;
        }

        // RankingEntry는 유저id, 총자산, 순위로 구성
        List<RankingRedisRepository.RankingEntry> all =
                rankingRedisRepository.getTopRankings(competitionId, 0, total);

        all.forEach(entry -> {
            Ranking ranking = Ranking.createRanking(competitionId, entry.userId());
            ranking.finalize(RankTier.from(entry.rank(), total));
            rankingRepository.save(ranking);
        });
    }

    // 대회 종료 시, 이벤트를 수신할 떄 실행되는 메서드
    // 참가자들의 최종 자산을 redis에 반영하고 DB에 저장하는 메서드(finalizeRankings)를 호출
    @Override
    @Transactional
    public void finalizeRankingsWithValuations(
            UUID competitionId,
            List<TotalAssetCalcuatedEvent.ParticipantTotalAsset> valuations
    ) {
        // 1. 최종 총자산을 Redis에 반영 (자산 서비스가 계산한 최종값으로 덮어쓰기)
        valuations.forEach(v ->
                rankingRedisRepository.upsertScore(competitionId, v.userId(), v.totalAsset())
        );

        // 2. Redis 기반으로 최종 순위 계산 후 DB에 저장
        finalizeRankings(competitionId);
    }

    // ── Search ────────────────────────────────────────────────────────────────

    @Override
    public List<RankingResult> findTopRankings(UUID competitionId, int page, int size) {
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
    public RankingResult findMyRanking(UUID competitionId, UUID userId) {
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
}
