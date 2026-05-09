package io.antcamp.assetservice.application.service;

import io.antcamp.assetservice.application.dto.command.CreateAccountCommand;
import io.antcamp.assetservice.application.dto.query.AccountResult;
import io.antcamp.assetservice.domain.exception.AccountNotFoundException;
import io.antcamp.assetservice.domain.exception.InvalidAmountException;
import io.antcamp.assetservice.domain.exception.UnauthorizedAccountAccessException;
import io.antcamp.assetservice.domain.model.Account;
import io.antcamp.assetservice.domain.repository.AccountRepository;
import io.antcamp.assetservice.presentation.dto.response.BalanceResponse;
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
                command.getInitialAmount(),
                command.getCompetitionId(),
                command.getCompetitionName()
        );
        return accountRepository.save(newAccount).getAccountId();
    }

    @Transactional
    public BalanceResponse deposit(UUID accountId, Long amount) {
        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException("계좌를 찾을 수 없습니다."));

        if (account.isEnded()) {
            throw new InvalidAmountException("종료된 대회 계좌는 거래할 수 없습니다.");
        }

        account.deposit(amount);
        accountRepository.save(account);

        return new BalanceResponse(accountId, account.getAccountAmount());
    }

    @Transactional
    public BalanceResponse withdraw(UUID accountId, Long amount) {
        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException("계좌를 찾을 수 없습니다."));

        if (account.isEnded()) {
            throw new InvalidAmountException("종료된 대회 계좌는 거래할 수 없습니다.");
        }

        account.withdraw(amount);
        accountRepository.save(account);
        return new BalanceResponse(accountId, account.getAccountAmount());
    }

    @Transactional(readOnly = true)
    public AccountResult getAccount(UUID accountId, UUID requesterUserId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("계좌를 찾을 수 없습니다."));

        if (!account.getUserId().equals(requesterUserId)) {
            throw new UnauthorizedAccountAccessException("해당 계좌에 접근할 권한이 없습니다.");
        }

        return new AccountResult(
                account.getAccountId(),
                account.getAccountNumber(),
                account.getAccountAmount()
        );
    }

    @Transactional(readOnly = true)
    Account getAccountDomain(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("계좌를 찾을 수 없습니다."));
    }

    @Transactional
    public void deleteByUserIdAndCompetitionId(UUID userId, UUID competitionId) {
        accountRepository.deleteByUserIdAndCompetitionId(userId, competitionId);
    }

    @Transactional
    public void deleteAllByCompetitionId(UUID competitionId) {
        accountRepository.deleteAllByCompetitionId(competitionId);
    }
}