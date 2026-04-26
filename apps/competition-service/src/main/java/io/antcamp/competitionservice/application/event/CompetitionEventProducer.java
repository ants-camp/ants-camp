package io.antcamp.competitionservice.application.event;

import io.antcamp.competitionservice.domain.event.CompetitionStartedPayload;

/**
 * 대회 도메인 이벤트 발행 인터페이스. 응용 계층은 이 인터페이스에만 의존하고, 실제 발행 메커니즘(Kafka 등)은 인프라 계층에서 구현한다.
 */
public interface CompetitionEventProducer {

    void publishCompetitionStarted(CompetitionStartedPayload payload);
}
