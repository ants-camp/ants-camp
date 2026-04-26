package io.antcamp.tradeservice.infrastructure.dto;

/**
 * 종목 검색 결과 응답 DTO (국내주식 전용)
 */
public record StockSearchResponse(
        String code,
        String name,
        String market   // KOSPI | KOSDAQ
) {}
