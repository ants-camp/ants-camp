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
    private UUID competitionId;
    private String competitionName;
    private boolean isEnded;

    public Account(UUID accountId, UUID userId, String accountNumber, AccountType type,
                   Long accountAmount, UUID competitionId, String competitionName, boolean isEnded) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.accountAmount = accountAmount;
        this.competitionId = competitionId;
        this.competitionName = competitionName;
        this.isEnded = isEnded;
    }

    public static Account create(UUID userId, String generatedNumber, AccountType type,
                                 Long initialAmount, UUID competitionId, String competitionName) {
        return new Account(
                UUID.randomUUID(),
                userId,
                generatedNumber,
                type,
                initialAmount,
                competitionId,
                competitionName,
                false
        );
    }

    public void end() {
        this.isEnded = true;
    }

    public void deposit(Long amount) {
        if (amount == null || amount <= 0) {
            throw new InvalidAmountException("입금액은 0보다 커야 합니다. (입력된 금액: " + amount + ")");
        }
        if (this.accountAmount == null) {
            throw new IllegalStateException("계좌 잔액 상태가 유효하지 않습니다.");
        }
        try {
            this.accountAmount = Math.addExact(this.accountAmount, amount);
        } catch (ArithmeticException e) {
            throw new InvalidAmountException("입금 후 잔액이 허용 범위를 초과합니다.");
        }
    }

    public void withdraw(Long amount) {
        if (amount == null || amount <= 0) {
            throw new InvalidAmountException("출금액은 0보다 커야 합니다. (입력된 금액: " + amount + ")");
        }
        if (this.accountAmount == null) {
            throw new IllegalStateException("계좌 잔액 상태가 유효하지 않습니다.");
        }
        if (this.accountAmount < amount) {
            throw new InsufficientBalanceException("잔액이 부족합니다. (현재 잔액: " + this.accountAmount + ", 출금 요청액: " + amount + ")");
        }
        this.accountAmount = Math.subtractExact(this.accountAmount, amount);
    }
}
