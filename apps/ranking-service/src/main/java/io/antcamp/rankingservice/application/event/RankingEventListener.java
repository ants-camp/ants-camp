package io.antcamp.rankingservice.application.event;

import io.antcamp.rankingservice.domain.repository.RankingRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingEventListener {

    private final RankingRedisRepository rankingRedisRepository;

    /**
     * DB 커밋 완료 후 Redis를 최종 순위값으로 동기화한다.
     * DB가 원천 데이터이고, Redis는 DB 상태를 따라가는 팔로워.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRankingFinalized(RankingFinalizedEvent event) {
        log.info("[Redis] 최종 랭킹 동기화 시작. competitionId={}, count={}",
                event.competitionId(), event.valuations().size());

        event.valuations().forEach(v ->
                rankingRedisRepository.upsertScore(event.competitionId(), v.userId(), v.totalAsset())
        );

        log.info("[Redis] 최종 랭킹 동기화 완료. competitionId={}", event.competitionId());
    }
}
