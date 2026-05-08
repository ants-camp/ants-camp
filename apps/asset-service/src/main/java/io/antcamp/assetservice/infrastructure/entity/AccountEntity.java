package io.antcamp.assetservice.infrastructure.entity;

import common.entity.BaseEntity;
import io.antcamp.assetservice.domain.model.Account;
import io.antcamp.assetservice.domain.model.AccountType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "p_accounts")
@SQLRestriction("deleted_at is NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class AccountEntity extends BaseEntity {

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

    public static AccountEntity from(Account account) {
        return AccountEntity.builder()
                .accountId(account.getAccountId())
                .userId(account.getUserId())
                .accountNumber(account.getAccountNumber())
                .type(account.getType())
                .accountAmount(account.getAccountAmount())
                .competitionId(account.getCompetitionId())
                .competitionName(account.getCompetitionName())
                .isEnded(account.isEnded())
                .build();
    }

    public Account toDomain() {
        return new Account(accountId, userId, accountNumber, type, accountAmount,
                competitionId, competitionName, isEnded);
    }
}