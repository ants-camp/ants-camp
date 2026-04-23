package io.antcamp.assetservice.domain.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public class Account {
    private UUID accountId;
    private UUID userId;
    private String accountNumber;
    private AccountType type;
    private Long accountAmount;

    public Account(UUID accountId, UUID userId, String accountNumber, AccountType type, Long accountAmount) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.accountAmount = accountAmount;
    }

    public static Account create(UUID userId, String generatedNumber, AccountType type, Long initialAmount) {
        Long finalAmount;
        if (type == AccountType.PERSONAL){
            finalAmount = 10_000_000L;
        } else {
            finalAmount = initialAmount;
        }
        return new Account(
                UUID.randomUUID(),
                userId,
                generatedNumber,
                (type != null) ? type : AccountType.PERSONAL,
                finalAmount
        );
    }
}
