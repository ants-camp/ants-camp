package io.antcamp.tradeservice.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MinutePriceOutput2List(
        @JsonProperty("output2")
        List<MinutePriceOutput2> output2
) {
}
