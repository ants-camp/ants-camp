package io.antcamp.assetservice.application.dto.query;

import java.util.UUID;

public record BalanceResult(UUID accountId, Long balance) {}