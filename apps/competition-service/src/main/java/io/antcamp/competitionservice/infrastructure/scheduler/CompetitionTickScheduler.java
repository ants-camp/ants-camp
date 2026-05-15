package io.antcamp.competitionservice.infrastructure.scheduler;

import io.antcamp.competitionservice.application.event.CompetitionEventProducer;
import io.antcamp.competitionservice.domain.event.CompetitionTicked;
import io.antcamp.competitionservice.domain.repository.CompetitionRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitionTickScheduler {

    private final CompetitionRepository competitionRepository;
    private final CompetitionEventProducer competitionEventProducer;

    /**
     * 매분 00초마다 진행 중인(ONGOING) 대회들의 틱 이벤트를 발행한다. 자산 서비스가 컨슘하여 각 대회 계좌들의 총자산을 계산하고 Redis Sorted Set에 랭킹을 반영한다.
     */
    @Scheduled(cron = "0 * * * * *")
    public void publishCompetitionTicks() {
        List<UUID> ongoingIds = competitionRepository.findAllOngoingIds();

        if (ongoingIds.isEmpty()) {
            log.warn("[Scheduler] ONGOING 상태인 대회가 없습니다. 틱 이벤트 발행 생략.");
            return;
        }

        log.info("[Scheduler] CompetitionTick 발행 시작. 진행 중인 대회 수={}", ongoingIds.size());

        ongoingIds.forEach(competitionId -> {
            try {
                competitionEventProducer.publishCompetitionTicked(new CompetitionTicked(competitionId));
            } catch (Exception e) {
                log.warn("[Scheduler] CompetitionTick 발행 실패. competitionId={}", competitionId, e);
            }
        });
    }
}
