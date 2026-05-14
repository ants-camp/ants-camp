package io.antcamp.assetservice.presentation.dto.response;

import java.util.UUID;

public record BalanceResponse(UUID accountId, Long balance) {}