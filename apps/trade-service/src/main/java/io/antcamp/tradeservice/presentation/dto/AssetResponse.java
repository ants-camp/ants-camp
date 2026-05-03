package io.antcamp.tradeservice.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.antcamp.tradeservice.infrastructure.dto.Holdings;
import jakarta.persistence.JoinColumn;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AssetResponse(
        UUID userId,
        String tradeType,
        LocalDateTime tradeAt,
        String stockCode,
        Integer stockAmount,
        Long stockPrice
) {
}
