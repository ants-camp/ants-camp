package io.antcamp.assetservice.presentation.dto.response;

import java.util.UUID;

public record AccountResponse(UUID accountId) {
    public static AccountResponse from(UUID id) {
        return new AccountResponse(id);
    }
}