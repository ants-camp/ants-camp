package io.antcamp.tradeservice.application.scheduler;

import io.antcamp.tradeservice.application.service.TradeService;
import io.antcamp.tradeservice.domain.model.Trade;
import io.antcamp.tradeservice.domain.model.TradeStatus;
import io.antcamp.tradeservice.domain.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

/**
 * 미체결 지정가 주문 처리 스케줄러.
 *
 * <p>동작 방식
 * <ol>
 *   <li>10초마다 DB에서 status=PENDING & orderType=LIMIT 주문 조회</li>
 *   <li>각 주문의 현재가와 지정가 조건 비교</li>
 *   <li>조건 충족 → 즉시 체결 처리 (AssetClient 호출 + Kafka 발행)</li>
 *   <li>장 마감(15:30) 이후 → 잔여 미체결 주문 전부 CANCELLED 처리</li>
 * </ol>
 *
 * <p>Redis 캐시를 우선 활용하므로 KIS API 직접 호출은 최소화됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LimitOrderScheduler {

    private final TradeService tradeService;
    private final TradeRepository tradeRepository;

    /** 장 마감 시각 (한국 증권시장 기준 15:30) */
    private static final LocalTime MARKET_CLOSE = LocalTime.of(15, 30);

    /**
     * 10초마다 미체결 지정가 주문 체결 시도.
     * fixedDelay — 이전 실행 완료 후 10초 대기 (동시 실행 방지).
     */
    @Scheduled(fixedDelay = 10_000)
    public void checkAndExecuteLimitOrders() {
        LocalTime now = LocalTime.now();

        // 장 마감 이후 — 미체결 주문 자동 취소
        if (now.isAfter(MARKET_CLOSE)) {
            cancelAllPendingAfterMarketClose();
            return;
        }

        // 장 시간 중 — 조건 충족 주문 체결 시도
        try {
            tradeService.executePendingLimitOrders();
        } catch (Exception e) {
            log.error("[LimitOrderScheduler] 스케줄러 실행 중 예외 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 장 마감 이후 남은 PENDING 지정가 주문을 CANCELLED 처리.
     * 매일 15:30 이후 최초 스케줄 실행 시 한 번만 실제 취소 대상이 존재하며,
     * 이후에는 findPendingLimitOrders() 가 빈 리스트를 반환하므로 부하 없음.
     */
    private void cancelAllPendingAfterMarketClose() {
        List<Trade> pendingOrders = tradeRepository.findPendingLimitOrders();
        if (pendingOrders.isEmpty()) return;

        log.info("[LimitOrderScheduler] 장 마감 — 미체결 지정가 주문 {}건 자동 취소", pendingOrders.size());
        for (Trade order : pendingOrders) {
            try {
                tradeRepository.updateStatus(Trade.updateCancelled(order));
                log.info("[LimitOrderScheduler] 자동 취소 — tradeId={} {} {}주",
                        order.tradeId(), order.tradeType(), order.stockAmount());
            } catch (Exception e) {
                log.error("[LimitOrderScheduler] 취소 실패 — tradeId={} error={}",
                        order.tradeId(), e.getMessage());
            }
        }
    }
}
