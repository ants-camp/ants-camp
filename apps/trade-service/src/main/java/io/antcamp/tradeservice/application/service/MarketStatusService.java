package io.antcamp.tradeservice.application.service;

import io.antcamp.tradeservice.infrastructure.dto.MarketStatusResponse;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * 장 운영 상태 서비스
 *
 * KRX 정규 장 시간: 09:00 ~ 15:30 KST (월~금, 공휴일 제외)
 * 공휴일은 간소화를 위해 주말만 처리하며, 실제 서비스에서는
 * KIS REST API (NWDAY_APLY_WEEK_CLS_CODE) 로 공휴일 목록을 조회해야 함.
 */
@Service
public class MarketStatusService {

    private static final ZoneId    KST   = ZoneId.of("Asia/Seoul");
    private static final LocalTime OPEN  = LocalTime.of(9, 0);
    private static final LocalTime CLOSE = LocalTime.of(15, 30);

    /**
     * 현재 KST 시각 기준 장 운영 상태를 반환한다.
     */
    public MarketStatusResponse getStatus() {
        ZonedDateTime now  = ZonedDateTime.now(KST);
        LocalDate     date = now.toLocalDate();
        LocalTime     time = now.toLocalTime();
        DayOfWeek     dow  = date.getDayOfWeek();

        // ① 주말 → 휴장
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return new MarketStatusResponse(
                    "HOLIDAY", "09:00", "15:30", true,
                    "주말 휴장 — 다음 거래일을 확인하세요"
            );
        }

        // ② 장전
        if (time.isBefore(OPEN)) {
            return new MarketStatusResponse(
                    "BEFORE_OPEN", "09:00", "15:30", false,
                    "장 시작 전 (09:00 KST 개장)"
            );
        }

        // ③ 장중
        if (!time.isAfter(CLOSE)) {
            return new MarketStatusResponse(
                    "OPEN", "09:00", "15:30", false,
                    "장 운영 중"
            );
        }

        // ④ 장후
        return new MarketStatusResponse(
                "AFTER_CLOSE", "09:00", "15:30", false,
                "장 마감 (15:30 KST 종료)"
        );
    }
}
