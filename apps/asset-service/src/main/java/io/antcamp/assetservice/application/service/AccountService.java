package io.antcamp.assetservice.application.service;

import io.antcamp.assetservice.application.dto.command.CreateAccountCommand;
import io.antcamp.assetservice.domain.exception.AccountNotFoundException;
import io.antcamp.assetservice.domain.model.Account;
import io.antcamp.assetservice.domain.model.AccountType;
import io.antcamp.assetservice.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public UUID createAccount(CreateAccountCommand command) {
        String accountNumber = UUID.randomUUID().toString();
        Account newAccount = Account.create(
                command.getUserId(),
                accountNumber,
                command.getType(),
                command.getInitialAmount()
        );
        return accountRepository.save(newAccount).getAccountId();
    }

    @Transactional
    public void deposit(UUID accountId, Long amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("계좌를 찾을 수 없습니다. ID: " + accountId));
        account.deposit(amount);
        accountRepository.save(account);
    }

    @Transactional
    public void withdraw(UUID accountId, Long amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("계좌를 찾을 수 없습니다. ID: " + accountId));
        account.withdraw(amount);
        accountRepository.save(account);
    }
}
