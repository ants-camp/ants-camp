package io.antcamp.rankingservice.application;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.rankingservice.application.dto.RankingResult;
import io.antcamp.rankingservice.application.event.RankingFinalizedEvent;
import io.antcamp.rankingservice.domain.event.TotalAssetCalcuatedEvent;
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

    // 매매 체결 시 랭킹 저장(갱신)
    @Override
    public void updateLiveRanking(UUID competitionId, UUID userId, Double totalAsset) {
        log.info("[Redis 저장 시작] upsertScore | competitionId={} userId={} totalAsset={}",
                competitionId, userId, totalAsset);
        rankingRedisRepository.upsertScore(competitionId, userId, totalAsset);
        log.info("[Redis 저장 완료] upsertScore | competitionId={} userId={} totalAsset={}",
                competitionId, userId, totalAsset);
    }

    // 대회 종료 시 최종 랭킹을 DB에 저장하는 메서드(DB에 관리자가 수동으로 저장할 때 사용)
    @Override
    @Transactional
    public int finalizeRankings(UUID competitionId) {
        long totalCount = rankingRedisRepository.getTotalCount(competitionId);
        if (totalCount == 0) {
            return 0;
        }

        // RankingEntry는 유저id, 총자산, 순위로 구성, 전체 참가자의 랭킹 목록 조회
        List<RankingRedisRepository.RankingEntry> all =
                rankingRedisRepository.getTopRankings(competitionId, 0, totalCount);

        // 참가자마다 최종 순위를 기록하고 isFinalized = true로 저장
        all.forEach(entry -> {
            Ranking ranking = Ranking.createRanking(competitionId, entry.userId());
            ranking.finalize(RankTier.from(entry.rank(), totalCount));
            rankingRepository.save(ranking);
        });

        return all.size();
    }

    // 대회 종료 이벤트 수신 시 최종 순위를 DB에 저장하고, 커밋 후 Redis를 동기화하는 메서드
    @Override
    @Transactional
    public void finalizeRankingsWithValuations(
            UUID competitionId,
            List<TotalAssetCalcuatedEvent.ParticipantTotalAsset> valuations
    ) {
        if (valuations.isEmpty()) {
            return;
        }

        long totalCount = valuations.size();

        // 1. valuations를 자산 내림차순 정렬 후 DB에 직접 저장 (Redis 거치지 않음)
        // Redis는 진행 중 전광판용이고, 최종 결과의 원천은 자산 서비스가 준 확정 데이터
        List<TotalAssetCalcuatedEvent.ParticipantTotalAsset> sorted = valuations.stream()
                .sorted((a, b) -> Double.compare(b.totalAsset(), a.totalAsset()))
                .toList();

        // 참가자마다 랭킹을 기록하고 isFinalized = true로 저장
        for (int i = 0; i < sorted.size(); i++) {
            var v = sorted.get(i);
            // 랭킹 데이터를 생성
            Ranking ranking = Ranking.createRanking(competitionId, v.userId());
            // 최중 순위를 기록하고, isFinalized = true로 변경
            ranking.finalize(RankTier.from((long) i + 1, totalCount)); // 1-based rank
            rankingRepository.save(ranking);
        }

        // 2. DB 커밋 완료 후 Redis를 최종값으로 동기화 (AFTER_COMMIT 리스너가 처리)
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
    public RankingResult findMyRanking(UUID competitionId, UUID userId) {
        // getRank(), getScore() 모두 userId 기반으로 직접 조회하므로
        // 다른 참가자의 순위 변경과 무관하게 항상 정확한 값을 반환한다.
        long rank0based = rankingRedisRepository.getRank(competitionId, userId);
//        if (rank0based < 0) {
//            throw new BusinessException(ErrorCode.INVALID_INPUT);
//        }
        Double totalAsset = rankingRedisRepository.getScore(competitionId, userId);
//        if (totalAsset == null) {
//            throw new BusinessException(ErrorCode.INVALID_INPUT);
//        }
        long rank1based = rank0based + 1;
        return new RankingResult(userId, totalAsset, rank1based);
    }
}
