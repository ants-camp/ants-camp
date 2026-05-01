package io.antcamp.assetservice.infrastructure.messaging.kafka.consumer;

import io.antcamp.assetservice.application.dto.command.CreateAccountCommand;
import io.antcamp.assetservice.application.service.AccountService;
import io.antcamp.assetservice.domain.model.AccountType;
import io.antcamp.assetservice.infrastructure.messaging.kafka.payload.CompetitionRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CompetitionEventConsumer {

    private final AccountService accountService;

    @Transactional
    @KafkaListener(
            topics = "${topics.competition.started}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleCompetitionRegistered(CompetitionRegisteredEvent payload) {

        AccountType accountType = AccountType.valueOf(payload.competitionType().toUpperCase());

        CreateAccountCommand command = new CreateAccountCommand(
                payload.userId(),
                accountType,
                (long) payload.firstSeed(),
                payload.competitionId(),
                payload.competitionName()
        );

        accountService.createAccount(command);
    }
}