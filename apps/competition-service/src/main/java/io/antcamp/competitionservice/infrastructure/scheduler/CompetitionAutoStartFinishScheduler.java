package io.antcamp.competitionservice.infrastructure.scheduler;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.competitionservice.application.CompetitionService;
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
public class CompetitionAutoStartFinishScheduler {

    private final CompetitionRepository competitionRepository;
    private final CompetitionService competitionService;

    /**
     * 매분 대회 시작 시간이 지난 PREPARING 대회를 자동으로 ONGOING으로 전환한다. 최소 참가 인원 미달 시 대회를 자동 취소한다.
     */
    @Scheduled(cron = "0 * * * * *")
    public void autoStartCompetitions() {
        List<UUID> ids = competitionRepository.findAllIdsReadyToStart();

        if (ids.isEmpty()) {
            return;
        }

        log.info("[Scheduler] 자동 시작 대상 대회 수={}", ids.size());

        for (UUID competitionId : ids) {
            try {
                competitionService.startCompetition(competitionId);
                log.info("[Scheduler] 대회 자동 시작 완료. competitionId={}", competitionId);
            } catch (BusinessException e) {
                if (e.getErrorCode() == ErrorCode.COMPETITION_MIN_PARTICIPANTS_NOT_MET) {
                    // 최소 인원 미달 → 자동 취소
                    try {
                        competitionService.cancelCompetition(competitionId);
                        log.warn("[Scheduler] 최소 인원 미달로 대회 자동 취소. competitionId={}", competitionId);
                    } catch (Exception cancelEx) {
                        log.error("[Scheduler] 대회 자동 취소 실패. competitionId={}", competitionId, cancelEx);
                    }
                } else {
                    log.error("[Scheduler] 대회 자동 시작 실패. competitionId={}", competitionId, e);
                }
            }
        }
    }

    /**
     * 매분 대회 종료 시간이 지난 ONGOING 대회를 자동으로 FINISHED로 전환한다.
     */
    @Scheduled(cron = "0 * * * * *")
    public void autoFinishCompetitions() {
        List<UUID> ids = competitionRepository.findAllIdsReadyToFinish();

        if (ids.isEmpty()) {
            return;
        }

        log.info("[Scheduler] 자동 종료 대상 대회 수={}", ids.size());

        for (UUID competitionId : ids) {
            try {
                competitionService.finishCompetition(competitionId);
                log.info("[Scheduler] 대회 자동 종료 완료. competitionId={}", competitionId);
            } catch (Exception e) {
                log.error("[Scheduler] 대회 자동 종료 실패. competitionId={}", competitionId, e);
            }
        }
    }
}
