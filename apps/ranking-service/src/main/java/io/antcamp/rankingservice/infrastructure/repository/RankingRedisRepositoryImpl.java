package io.antcamp.rankingservice.infrastructure.repository;

import io.antcamp.rankingservice.domain.repository.RankingRedisRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RankingRedisRepositoryImpl implements RankingRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "ranking:competition:";

    private String key(UUID competitionId) {
        return KEY_PREFIX + competitionId;
    }

    @Override
    public void upsertScore(UUID competitionId, UUID userId, Double totalAsset) {
        redisTemplate.opsForZSet()
                .add(key(competitionId), userId.toString(), totalAsset);
    }

    @Override
    public long getRank(UUID competitionId, UUID userId) {
        Long rank = redisTemplate.opsForZSet()
                .reverseRank(key(competitionId), userId.toString());
        return rank != null ? rank : -1L;
    }

    @Override
    public long getTotalCount(UUID competitionId) {
        Long count = redisTemplate.opsForZSet().size(key(competitionId));
        return count != null ? count : 0L;
    }

    @Override
    public List<RankingEntry> getTopRankings(UUID competitionId, long offset, long count) {
        Set<TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key(competitionId), offset, offset + count - 1);
        if (tuples == null) {
            return List.of();
        }

        AtomicLong rankCounter = new AtomicLong(offset + 1); // 1-based
        return tuples.stream()
                .map(t -> new RankingEntry(
                        UUID.fromString(t.getValue()),
                        t.getScore(),
                        rankCounter.getAndIncrement()))
                .toList();
    }
}
