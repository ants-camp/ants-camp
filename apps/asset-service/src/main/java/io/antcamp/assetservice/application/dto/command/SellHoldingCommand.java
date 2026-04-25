package io.antcamp.assetservice.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SellHoldingCommand {

    private UUID accountId;
    private String stockCode;
    private Integer stockAmount;
}