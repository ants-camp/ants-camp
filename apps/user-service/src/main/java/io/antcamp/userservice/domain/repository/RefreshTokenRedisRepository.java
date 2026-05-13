package io.antcamp.userservice.domain.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

    private static final String KEY_PREFIX = "refresh_token:";

    private final StringRedisTemplate stringRedisTemplate;

    public void save(UUID userId, String refreshToken, Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);

        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }

        String key = KEY_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(key, refreshToken, ttl);
    }

    public Optional<String> findByUserId(UUID userId) {
        String key = KEY_PREFIX + userId;
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
    }

    public boolean existsByUserId(UUID userId) {
        String key = KEY_PREFIX + userId;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }


    public void deleteByUserId(UUID userId) {
        String key = KEY_PREFIX + userId;
        stringRedisTemplate.delete(key);
    }
}