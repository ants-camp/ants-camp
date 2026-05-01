package io.antcamp.userservice.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

import java.util.UUID;

@Entity
@Table(name = "p_refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(nullable = false)
    private Instant expiresAt;

    public boolean isExpired() {
        return this.expiresAt.isBefore(Instant.now());
    }
    public void updateToken(String token, Instant expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }
}