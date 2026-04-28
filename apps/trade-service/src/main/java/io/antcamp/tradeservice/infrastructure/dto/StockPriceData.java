package io.antcamp.tradeservice.infrastructure.dto;

import java.math.BigDecimal;

/**
 * KIS H0STCNT0 (국내주식 실시간 체결가) 파싱 결과
 *
 * 수신 포맷: 0|H0STCNT0|001|종목코드^체결시간^현재가^전일대비^등락률^체결거래량^...
 *
 * KIS 공식 필드 순서 (앞 6개만 사용):
 *  [0] 유가증권 단축 종목코드
 *  [1] 주식 체결 시간       (HHmmss)
 *  [2] 주식 현재가
 *  [3] 전일 대비            (+ 상승 / - 하락)
 *  [4] 전일 대비율          (소수점 2자리)
 *  [5] 체결 거래량
 */
public record StockPriceData(
        String stockCode,
        String tradeTime,
        long currentPrice,
        long priceChange,
        BigDecimal changeRate,
        long volume
) {
    /** 등락 방향 문자열 (UI 표시용) */
    public String direction() {
        if (priceChange > 0) return "UP";
        if (priceChange < 0) return "DOWN";
        return "FLAT";
    }
}
