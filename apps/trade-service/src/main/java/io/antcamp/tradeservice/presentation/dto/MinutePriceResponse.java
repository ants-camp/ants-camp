package io.antcamp.tradeservice.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public record MinutePriceResponse(
        @JsonProperty("output1")
        MinutePriceOutput1 minutePriceOutput1,
        @JsonProperty("output2")
        List<MinutePriceOutput2> minutePriceOutput2,
        @JsonProperty("rt_cd")
        String rtCd,
        @JsonProperty("msg_cd")
        String responseCode,
        @JsonProperty("msg1")
        String responseMessage
) {
    /** 폴백용 빈 응답 */
    public static MinutePriceResponse empty() {
        return new MinutePriceResponse(null, Collections.emptyList(), "1", "FALLBACK", "서비스 일시 불가");
    }
}
