package io.antcamp.tradeservice.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record MinutePriceRequestHeader(
        @JsonProperty("content-type")
        String contentType,
        @JsonProperty("authorization")
        String authorization,
        @JsonProperty("appkey")
        String appkey,
        @JsonProperty("appsecret")
        String appsecret,
        @JsonProperty("tr_id")
        String trId,
        @JsonProperty("custtype")
        String custtype,
        @JsonProperty("tr_cont")
        String trCont

) {
    public static Map<String, Object> create(String authorization, String appkey, String appsecret) {
        return Map.of(
                "content-type", "application/json; charset=utf-8",
                "authorization", authorization,   // 호출부에서 이미 "Bearer <token>" 형태로 전달
                "appkey", appkey,
                "appsecret", appsecret,
                "tr_id", "FHKST03010230",
                "custtype", "P",
                "tr_cont", "N"                    // "trCont" → "tr_cont" (KIS 헤더 필드명)
        );
    }
}
