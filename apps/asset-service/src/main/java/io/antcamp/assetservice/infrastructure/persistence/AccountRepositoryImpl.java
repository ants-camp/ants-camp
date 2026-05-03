package io.antcamp.assetservice.infrastructure.persistence;

import io.antcamp.assetservice.domain.model.Account;
import io.antcamp.assetservice.domain.repository.AccountRepository;
import io.antcamp.assetservice.infrastructure.entity.AccountEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

    private final JpaAccountRepository jpaAccountRepository;

    @Override
    public Account save(Account account) {
        AccountEntity entity = AccountEntity.from(account);
        AccountEntity savedEntity = jpaAccountRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Account> findById(UUID accountId) {
        return jpaAccountRepository.findById(accountId)
                .map(AccountEntity::toDomain);
    }

    @Override
    public Optional<Account> findByIdWithLock(UUID accountId) {
        return jpaAccountRepository.findByIdWithLock(accountId)
                .map(AccountEntity::toDomain);
    }

    @Override
    public List<Account> findAllByCompetitionId(UUID competitionId) {
        return jpaAccountRepository.findAllByCompetitionId(competitionId)
                .stream()
                .map(AccountEntity::toDomain)
                .toList();
    }
}