package io.antcamp.assetservice.application.dto.command;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BuyHoldingCommand {

    @NotNull
    private UUID accountId;

    @NotBlank
    private String stockCode;

    @NotNull
    @Positive
    private Integer stockAmount;

    @NotNull
    @Positive
    private Long buyPrice;

    @NotNull
    private UUID userId;
}