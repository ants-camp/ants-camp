package io.antcamp.assetservice.application.dto.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AccountResult {
    private UUID accountId;
    private String accountNumber;
    private Long accountAmount;
    private UUID competitionId;   // 어느 대회 계좌인지 (JMeter가 현재 대회 계좌를 정확히 찾기 위해 필요)
    private boolean isEnded;      // 종료된 계좌인지 (isEnded=true 계좌로 매수 시도 → INVALID_AMOUNT 방지)
}
