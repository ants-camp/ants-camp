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
    private AccountType type;

    private Long accountAmount;

    private AccountEntity(UUID accountId, UUID userId, String accountNumber, AccountType type, Long accountAmount) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.accountAmount = accountAmount;
    }

    public static AccountEntity from(Account account) {
        return new AccountEntity(
                account.getAccountId(),
                account.getUserId(),
                account.getAccountNumber(),
                account.getType(),
                account.getAccountAmount()
        );
    }

    public Account toDomain() {
        return new Account(accountId, userId, accountNumber, type, accountAmount);
    }
}