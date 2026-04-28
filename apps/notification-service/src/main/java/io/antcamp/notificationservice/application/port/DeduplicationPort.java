package io.antcamp.notificationservice.application.port;

import java.time.Duration;

public interface DeduplicationPort {
    boolean tryReserve(String key, Duration ttl);
}