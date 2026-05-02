package io.antcamp.competitionservice.application.event;

import io.antcamp.competitionservice.domain.event.CompetitionAbortedEvent;
import io.antcamp.competitionservice.domain.event.CompetitionCancelledEvent;
import io.antcamp.competitionservice.domain.event.CompetitionEndedEvent;
import io.antcamp.competitionservice.domain.event.CompetitionRegisteredEvent;
import io.antcamp.competitionservice.domain.event.CompetitionTicked;

/**
 * 대회 도메인 이벤트 발행 인터페이스.
 * 응용 계층은 이 인터페이스에만 의존하고, 실제 발행 메커니즘(Kafka 등)은 인프라 계층에서 구현한다.
 */
public interface CompetitionEventProducer {

    /** 참가자가 대회에 신청할 때 발행 → 자산 서비스가 컨슘하여 대회 전용 계좌 생성 */
    void publishCompetitionRegistered(CompetitionRegisteredEvent event);

    /** 대회가 종료될 때 발행 → 자산 서비스가 컨슘하여 최종 총자산 계산 */
    void publishCompetitionEnded(CompetitionEndedEvent event);

    /** 참가자가 대회 신청을 취소할 때 발행 → 자산 서비스가 컨슘하여 대회 전용 계좌 정리 */
    void publishCompetitionCancelled(CompetitionCancelledEvent event);

    /** 대회 자체가 취소될 때 발행 → 참가자 계좌 정리 등 후속 처리가 필요한 서비스가 컨슘 */
    void publishCompetitionAborted(CompetitionAbortedEvent event);

    /** 1분마다 진행 중인 대회에서 발행 → 자산 서비스가 컨슘하여 총자산 계산 후 Redis 랭킹 반영 */
    void publishCompetitionTicked(CompetitionTicked event);
}
