package io.antcamp.assetservice.domain.repository;

import io.antcamp.assetservice.domain.model.Account;

public interface AccountRepository {

    //계좌 생성
    Account save(Account account);
}
