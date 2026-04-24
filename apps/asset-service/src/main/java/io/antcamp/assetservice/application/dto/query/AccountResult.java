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
}