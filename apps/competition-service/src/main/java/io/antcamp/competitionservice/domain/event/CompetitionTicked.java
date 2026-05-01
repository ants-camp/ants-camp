package io.antcamp.competitionservice.domain.event;

import java.util.UUID;

/**
 * 진행 중인 대회에서 1분마다 발행되는 틱 이벤트.
 * 자산 서비스가 컨슘하여 해당 대회 계좌들의 총자산을 계산하고 Redis Sorted Set에 랭킹을 반영한다.
 */
public record CompetitionTicked(
        UUID competitionId
) {
}
