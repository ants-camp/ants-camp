package io.antcamp.tradeservice.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApprovalKeyResponse(
        @JsonProperty("approval_key")
        String approvalKey
) {
}
