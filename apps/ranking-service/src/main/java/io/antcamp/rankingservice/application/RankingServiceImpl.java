package io.antcamp.rankingservice.application;

import io.antcamp.rankingservice.application.dto.CompetitionHistoryResult;
import io.antcamp.rankingservice.application.dto.RankingResult;
import io.antcamp.rankingservice.application.event.RankingFinalizedEvent;
import io.antcamp.rankingservice.domain.event.TotalAssetCalculatedEvent;
import io.antcamp.rankingservice.domain.model.RankTier;
import io.antcamp.rankingservice.domain.model.Ranking;
import io.antcamp.rankingservice.domain.repository.RankingRedisRepository;
import io.antcamp.rankingservice.domain.repository.RankingRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final RankingRepository rankingRepository;
    private final RankingRedisRepository rankingRedisRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    // ── Update ────────────────────────────────────────────────────────────────

    // 관리자 수동 트리거용 - Redis 기준으로 최종 순위를 DB에 확정
    @Override
    @Transactional
    public int finalizeRankings(UUID competitionId) {
        long totalCount = rankingRedisRepository.getTotalCount(competitionId);
        if (totalCount == 0) {
            return 0;
        }

        List<RankingRedisRepository.RankingEntry> all =
                rankingRedisRepository.getTopRankings(competitionId, 0, totalCount);

        all.forEach(entry -> {
            Ranking ranking = Ranking.createRanking(competitionId, entry.userId());
            ranking.finalize(RankTier.from(entry.rank(), totalCount));
            rankingRepository.save(ranking);
        });

        return all.size();
    }

    @Override
    @Transactional
    public void finalizeRankingsWithValuations(
            UUID competitionId,
            List<TotalAssetCalculatedEvent.ParticipantTotalAsset> valuations
    ) {
        if (valuations.isEmpty()) {
            return;
        }

        long totalCount = valuations.size();

        // 자산 서비스가 전달한 확정 데이터를 내림차순 정렬해 DB에 직접 저장
        // Redis는 대회 진행 중 실시간 전광판용이므로 최종 결과의 원천은 이 valuations
        List<TotalAssetCalculatedEvent.ParticipantTotalAsset> sorted = valuations.stream()
                .sorted((a, b) -> Double.compare(b.totalAsset(), a.totalAsset()))
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            var v = sorted.get(i);
            Ranking ranking = Ranking.createRanking(competitionId, v.userId());
            ranking.finalize(RankTier.from((long) i + 1, totalCount));
            rankingRepository.save(ranking);
        }

        // DB 커밋 완료 후 Redis를 최종값으로 동기화 (AFTER_COMMIT 리스너가 처리)
        applicationEventPublisher.publishEvent(new RankingFinalizedEvent(competitionId, valuations));
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
    @Transactional(readOnly = true)
    public List<CompetitionHistoryResult> findMyHistory(UUID userId) {
        return rankingRepository.findAllByUserId(userId)
                .stream()
                .map(CompetitionHistoryResult::from)
                .toList();
    }

    @Override
    public RankingResult findMyRanking(UUID competitionId, UUID userId) {
        // getRank(), getScore() 모두 userId 기반으로 직접 조회하므로
        // 다른 참가자의 순위 변경과 무관하게 항상 정확한 값을 반환한다.
        long rank0based = rankingRedisRepository.getRank(competitionId, userId);
        Double totalAsset = rankingRedisRepository.getScore(competitionId, userId);
        long rank1based = rank0based + 1;
        return new RankingResult(userId, totalAsset, rank1based);
    }
}
