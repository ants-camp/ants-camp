package io.antcamp.tradeservice.infrastructure.dto;

/**
 * 장 운영 상태 응답 DTO
 *
 * status:
 *   BEFORE_OPEN  — 장전 (09:00 이전)
 *   OPEN         — 장중 (09:00 ~ 15:30)
 *   AFTER_CLOSE  — 장후 (15:30 이후)
 *   HOLIDAY      — 주말·공휴일 휴장
 */
public record MarketStatusResponse(
        String status,
        String openTime,
        String closeTime,
        boolean isHoliday,
        String message
) {}
