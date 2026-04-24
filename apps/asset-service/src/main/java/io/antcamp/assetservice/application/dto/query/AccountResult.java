package io.antcamp.assetservice.application.dto.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AccountResult {
    private UUID accountId;        // 계좌 고유 ID
    private String accountNumber;  // 계좌 번호
    private Long accountAmount;    // 현재 잔액
}