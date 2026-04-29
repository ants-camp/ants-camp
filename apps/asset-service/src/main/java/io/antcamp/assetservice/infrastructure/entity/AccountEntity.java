package io.antcamp.assetservice.infrastructure.entity;

import io.antcamp.assetservice.domain.model.Account;
import io.antcamp.assetservice.domain.model.AccountType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "p_accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountEntity {

    @Id
    private UUID accountId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Column(nullable = false)
    private Long accountAmount;

    @Column(nullable = true)
    private UUID competitionId;

    @Column(nullable = true)
    private String competitionName;

    @Column(nullable = false)
    private boolean isEnded;

    private AccountEntity(UUID accountId, UUID userId, String accountNumber, AccountType type,
                          Long accountAmount, UUID competitionId, String competitionName, boolean isEnded) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.accountAmount = accountAmount;
        this.competitionId = competitionId;
        this.competitionName = competitionName;
    }

    public static AccountEntity from(Account account) {
        return new AccountEntity(
                account.getAccountId(),
                account.getUserId(),
                account.getAccountNumber(),
                account.getType(),
                account.getAccountAmount(),
                account.getCompetitionId(),
                account.getCompetitionName(),
                account.isEnded()
        );
    }

    public Account toDomain() {
        return new Account(accountId, userId, accountNumber, type, accountAmount,
                competitionId, competitionName, isEnded);
    }
}