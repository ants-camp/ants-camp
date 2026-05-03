package io.antcamp.competitionservice.application.event;

import io.antcamp.competitionservice.domain.event.CompetitionCancelledEvent;
import io.antcamp.competitionservice.domain.event.CompetitionRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 대회 참가자 관련 이벤트를 수신하여 Kafka로 발행하는 리스너.
 * DB 커밋 완료 후에만 Kafka 발행을 보장한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitionParticipantEventListener {

    private final CompetitionEventProducer competitionEventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCompetitionRegistered(CompetitionRegisteredEvent event) {
        competitionEventProducer.publishCompetitionRegistered(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCompetitionCancelled(CompetitionCancelledEvent event) {
        competitionEventProducer.publishCompetitionCancelled(event);
    }
}
