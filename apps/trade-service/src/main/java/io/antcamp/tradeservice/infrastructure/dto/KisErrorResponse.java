package io.antcamp.tradeservice.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisErrorResponse(
        @JsonProperty("rt_cd")  String rtCd,
        @JsonProperty("msg_cd") String msgCd,
        @JsonProperty("msg1")   String message
) {
    // KIS API 에러 코드
    // EGW00201 : 토큰 만료 → 재시도 가능
    // EGW00304 : appsecret 유효하지 않음
    // EGW00121 : 초당 거래건수 초과 → 재시도 가능

    public boolean isRetryable() {
        return "EGW00201".equals(msgCd) || "EGW00121".equals(msgCd);
    }
}
