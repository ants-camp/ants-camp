package io.antcamp.assetservice.infrastructure.persistence;

import io.antcamp.assetservice.domain.model.Account;
import io.antcamp.assetservice.domain.repository.AccountRepository;
import io.antcamp.assetservice.infrastructure.entity.AccountEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}