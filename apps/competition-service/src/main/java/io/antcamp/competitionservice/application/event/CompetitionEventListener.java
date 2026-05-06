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
        competitionEventProducer.publishCompetitionEnded(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCompetitionAborted(CompetitionAbortedEvent event) {
        competitionEventProducer.publishCompetitionAborted(event);
    }
}
