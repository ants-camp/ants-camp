package io.antcamp.assetservice.domain.repository;

import io.antcamp.assetservice.domain.model.Account;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    //계좌 생성
    Account save(Account account);

    Optional<Account> findById(UUID accountId);
}
