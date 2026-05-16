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

    @Override
    public Optional<Account> findByUserIdAndCompetitionId(UUID userId, UUID competitionId) {
        return jpaAccountRepository.findByUserIdAndCompetitionId(userId, competitionId)
                .map(AccountEntity::toDomain);
    }

    @Override
    public void deleteByUserIdAndCompetitionId(UUID userId, UUID competitionId) {
        jpaAccountRepository
                .findByUserIdAndCompetitionId(userId, competitionId)
                .ifPresent(entity -> {
                    entity.softDelete("SYSTEM");
                    jpaAccountRepository.save(entity);
                });
    }

    @Override
    public void deleteAllByCompetitionId(UUID competitionId) {
        //현재 단일 트랜잭션으로 처리 중 - 규모 커질 시 성능 이슈 가능성 있음
        //계좌별 독립 트랜잭션으로 분리 필요
        List<AccountEntity> entities = jpaAccountRepository.findAllByCompetitionId(competitionId);
        entities.forEach(e -> e.softDelete("SYSTEM"));
        jpaAccountRepository.saveAll(entities);
    }

    @Override
    public List<Account> findAllByUserId(UUID userId) {
        return jpaAccountRepository.findAllByUserId(userId)
                .stream()
                .map(AccountEntity::toDomain)
                .toList();
    }
}