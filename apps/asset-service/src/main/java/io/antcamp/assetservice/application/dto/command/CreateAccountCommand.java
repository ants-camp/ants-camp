package io.antcamp.assetservice.application.dto.command;

import io.antcamp.assetservice.domain.model.AccountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountCommand {

    private UUID userId;
    private AccountType type;
    private Long initialAmount;
    private UUID competitionId;
    private String competitionName;
}