package io.antcamp.competitionservice.application.event;

import io.antcamp.competitionservice.domain.event.CompetitionAbortedEvent;
import io.antcamp.competitionservice.domain.event.CompetitionEndedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitionEventListener {
    private final CompetitionEventProducer competitionEventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCompetitionEnded(CompetitionEndedEvent event) {
        log.info("[이벤트 발행] CompetitionEnded | competitionId={} participantCount={}",
                event.competitionId(), event.participantUserIds().size());
        competitionEventProducer.publishCompetitionEnded(event);
        log.debug("[이벤트 발행 완료] CompetitionEnded | competitionId={}", event.competitionId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCompetitionAborted(CompetitionAbortedEvent event) {
        log.info("[이벤트 발행] CompetitionAborted | competitionId={} participantCount={}",
                event.competitionId(), event.participantUserIds().size());
        competitionEventProducer.publishCompetitionAborted(event);
        log.debug("[이벤트 발행 완료] CompetitionAborted | competitionId={}", event.competitionId());
    }
}
