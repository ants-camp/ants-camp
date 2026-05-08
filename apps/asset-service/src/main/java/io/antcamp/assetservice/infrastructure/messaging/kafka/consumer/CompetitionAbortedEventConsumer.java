package io.antcamp.assetservice.infrastructure.messaging.kafka.consumer;

import io.antcamp.assetservice.application.service.AccountService;
import io.antcamp.assetservice.domain.event.payload.CompetitionAbortedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitionAbortedEventConsumer {

    private final AccountService accountService;

    @Transactional
    @KafkaListener(
            topics = "${topics.competition.aborted}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "competitionAbortedFactory"
    )
    public void handleCompetitionAborted(CompetitionAbortedEvent payload) {
        log.info("[Kafka] CompetitionAbortedEvent 수신. competitionId={}, 참가자 수={}",
                payload.competitionId(), payload.participantUserIds().size());

        accountService.deleteAllByCompetitionId(payload.competitionId());

        log.info("[Kafka] 대회 취소 전체 계좌 softDelete 완료. competitionId={}",
                payload.competitionId());
    }
}