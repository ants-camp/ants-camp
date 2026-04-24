package io.antcamp.assetservice.domain.model;

import io.antcamp.assetservice.domain.exception.InsufficientBalanceException;
import io.antcamp.assetservice.domain.exception.InvalidAmountException;
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
        AccountType resolvedType = (type != null) ? type : AccountType.PERSONAL;
        Long finalAmount;
        if (resolvedType == AccountType.PERSONAL){
            finalAmount = 10_000_000L;
        } else {
            finalAmount = initialAmount;
        }
        return new Account(
                UUID.randomUUID(),
                userId,
                generatedNumber,
                resolvedType,
                finalAmount
        );
    }

    public void deposit(Long amount) {
        if (amount == null || amount <= 0) {
            throw new InvalidAmountException("입금액은 0보다 커야 합니다. (입력된 금액: " + amount + ")");
        }
        this.accountAmount += amount;
    }

    public void withdraw(Long amount) {
        if (amount == null || amount <= 0) {
            throw new InvalidAmountException("출금액은 0보다 커야 합니다. (입력된 금액: " + amount + ")");
        }
        if (this.accountAmount < amount) {
            throw new InsufficientBalanceException("잔액이 부족합니다. (현재 잔액: " + this.accountAmount + ", 출금 요청액: " + amount + ")");
        }
        this.accountAmount -= amount;
    }
}
