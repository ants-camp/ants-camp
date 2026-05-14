package io.antcamp.notificationservice.infrastructure.client.redis;

import io.antcamp.notificationservice.application.port.DeduplicationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisDeduplicationAdapter implements DeduplicationPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean tryReserve(String key, Duration ttl) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "1", ttl));
    }
}