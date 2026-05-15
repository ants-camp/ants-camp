package io.antcamp.assetservice.application.service;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.assetservice.application.dto.command.CreateAccountCommand;
import io.antcamp.assetservice.application.dto.query.AccountResult;
import io.antcamp.assetservice.application.dto.query.BalanceResult;
import io.antcamp.assetservice.domain.model.Account;
import io.antcamp.assetservice.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public UUID createAccount(CreateAccountCommand command) {
        log.info("[Account] 계좌 생성 요청. userId={}, type={}, competitionId={}",
                command.getUserId(), command.getType(), command.getCompetitionId());
        String accountNumber = UUID.randomUUID().toString();
        Account newAccount = Account.create(
                command.getUserId(),
                accountNumber,
                command.getType(),
                command.getInitialAmount(),
                command.getCompetitionId(),
                command.getCompetitionName()
        );
        UUID createdId = accountRepository.save(newAccount).getAccountId();
        log.info("[Account] 계좌 생성 완료. accountId={}", createdId);
        return createdId;
    }

    @Transactional
    public BalanceResult deposit(UUID accountId, Long amount) {
        log.info("[Account] 입금 요청. accountId={}, amount={}", accountId, amount);
        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)); // ✅ 변경

        if (account.isEnded()) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT); // ✅ 변경
        }

        account.deposit(amount);
        accountRepository.save(account);
        log.info("[Account] 입금 완료. accountId={}, 잔액={}", accountId, account.getAccountAmount());
        return new BalanceResult(accountId, account.getAccountAmount());
    }

    @Transactional
    public BalanceResult withdraw(UUID accountId, Long amount) {
        log.info("[Account] 출금 요청. accountId={}, amount={}", accountId, amount);
        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)); // ✅ 변경

        if (account.isEnded()) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT); // ✅ 변경
        }

        account.withdraw(amount);
        accountRepository.save(account);
        log.info("[Account] 출금 완료. accountId={}, 잔액={}", accountId, account.getAccountAmount());
        return new BalanceResult(accountId, account.getAccountAmount());
    }

    @Transactional(readOnly = true)
    public AccountResult getAccount(UUID accountId, UUID requesterUserId) {
        log.debug("[Account] 계좌 조회 요청. accountId={}, userId={}", accountId, requesterUserId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)); // ✅ 변경

        if (!account.getUserId().equals(requesterUserId)) {
            log.warn("[Account] 권한 없는 계좌 접근 시도. accountId={}, 요청userId={}, 계좌ownerId={}",
                    accountId, requesterUserId, account.getUserId());
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCOUNT_ACCESS); // ✅ 변경
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
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)); // ✅ 변경
    }

    /**
     * 내부 서비스 호출용 — 잔액만 반환. 권한 검증 생략.
     * `/internal/...` 경로로만 노출해야 함.
     */
    @Transactional(readOnly = true)
    public long getBalance(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("계좌를 찾을 수 없습니다."));
        return account.getAccountAmount() != null ? account.getAccountAmount() : 0L;
    }

    @Transactional
    public void deleteByUserIdAndCompetitionId(UUID userId, UUID competitionId) {
        log.info("[Account] 계좌 삭제 요청. userId={}, competitionId={}", userId, competitionId);
        accountRepository.deleteByUserIdAndCompetitionId(userId, competitionId);
        log.info("[Account] 계좌 삭제 완료. userId={}, competitionId={}", userId, competitionId);
    }

    @Transactional
    public void deleteAllByCompetitionId(UUID competitionId) {
        log.info("[Account] 대회 전체 계좌 삭제 요청. competitionId={}", competitionId);
        accountRepository.deleteAllByCompetitionId(competitionId);
        log.info("[Account] 대회 전체 계좌 삭제 완료. competitionId={}", competitionId);
    }

    @Transactional(readOnly = true)
    public List<AccountResult> getAccountsByUserId(UUID userId) {
        log.debug("[Account] 유저 계좌 목록 조회. userId={}", userId);

        List<AccountResult> results = accountRepository.findAllByUserId(userId)
                .stream()
                .map(account -> new AccountResult(
                        account.getAccountId(),
                        account.getAccountNumber(),
                        account.getAccountAmount()
                ))
                .toList();

        log.debug("[Account] 유저 계좌 목록 조회 완료. userId={}, 계좌수={}", userId, results.size());
        return results;
    }
}
