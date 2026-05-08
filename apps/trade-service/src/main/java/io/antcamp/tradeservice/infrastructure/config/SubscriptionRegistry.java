package io.antcamp.tradeservice.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 사용자별 실시간 종목 구독 레지스트리
 *
 * 역할:
 *  - 다수 사용자가 동일 종목을 구독해도 KIS 에는 단 1번만 구독 메시지를 전송 (레퍼런스 카운팅)
 *  - 모든 구독자가 해제되면 KIS 에 구독 해제 메시지를 전송
 *
 * 스레드 안전: ConcurrentHashMap 기반
 */
@Slf4j
@Component
public class SubscriptionRegistry {

    /** stockCode → 구독 중인 sessionId(또는 userId) 집합 */
    private final Map<String, Set<String>> stockToSessions = new ConcurrentHashMap<>();

    /** sessionId → 구독 중인 stockCode 집합 */
    private final Map<String, Set<String>> sessionToStocks = new ConcurrentHashMap<>();

    // ──────────────────────────────────────────────────────────────────────

    /**
     * 구독 등록
     *
     * @return true  = 이 종목의 첫 번째 구독 (KIS 에 구독 요청 필요)
     *         false = 이미 다른 사용자가 구독 중 (KIS 요청 불필요)
     */
    public boolean subscribe(String sessionId, String stockCode) {
        sessionToStocks
                .computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet())
                .add(stockCode);

        Set<String> sessions = stockToSessions
                .computeIfAbsent(stockCode, k -> ConcurrentHashMap.newKeySet());

        boolean isFirst = sessions.isEmpty();
        sessions.add(sessionId);

        log.info("[Registry] 구독 등록: session={}, stock={}, 구독자={}명 (first={})",
                sessionId, stockCode, sessions.size(), isFirst);
        return isFirst;
    }

    /**
     * 구독 해제
     *
     * @return true  = 마지막 구독자가 빠짐 (KIS 에 해제 요청 필요)
     *         false = 아직 다른 구독자가 있음 (KIS 요청 불필요)
     */
    public boolean unsubscribe(String sessionId, String stockCode) {
        Set<String> sessions = stockToSessions.get(stockCode);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                stockToSessions.remove(stockCode);
                log.info("[Registry] 구독 해제(마지막): session={}, stock={}", sessionId, stockCode);
                cleanSession(sessionId, stockCode);
                return true;
            }
        }
        cleanSession(sessionId, stockCode);
        log.info("[Registry] 구독 해제: session={}, stock={}, 남은 구독자={}명",
                sessionId, stockCode, sessions != null ? sessions.size() : 0);
        return false;
    }

    /**
     * 세션 종료 시 해당 세션의 모든 구독을 제거.
     *
     * @return 더 이상 구독자가 없어진 종목코드 집합 (KIS 해제 필요)
     */
    public Set<String> removeSession(String sessionId) {
        Set<String> stocks = sessionToStocks.remove(sessionId);
        if (stocks == null) return Collections.emptySet();

        Set<String> emptyStocks = ConcurrentHashMap.newKeySet();
        for (String stockCode : stocks) {
            Set<String> sessions = stockToSessions.get(stockCode);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    stockToSessions.remove(stockCode);
                    emptyStocks.add(stockCode);
                }
            }
        }
        log.info("[Registry] 세션 제거: session={}, 구독 종목={}개, KIS 해제 필요={}개",
                sessionId, stocks.size(), emptyStocks.size());
        return emptyStocks;
    }

    /** 특정 종목의 현재 구독자 수 */
    public int subscriberCount(String stockCode) {
        Set<String> s = stockToSessions.get(stockCode);
        return s == null ? 0 : s.size();
    }

    /** 전체 구독 중인 종목 코드 */
    public Set<String> allSubscribedStocks() {
        return Collections.unmodifiableSet(stockToSessions.keySet());
    }

    // ─── private ──────────────────────────────────────────────────────────

    private void cleanSession(String sessionId, String stockCode) {
        Set<String> stocks = sessionToStocks.get(sessionId);
        if (stocks != null) {
            stocks.remove(stockCode);
            if (stocks.isEmpty()) sessionToStocks.remove(sessionId);
        }
    }
}
