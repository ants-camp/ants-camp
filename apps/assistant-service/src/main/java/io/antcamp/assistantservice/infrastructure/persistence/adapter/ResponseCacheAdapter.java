package io.antcamp.assistantservice.infrastructure.persistence.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import io.antcamp.assistantservice.application.port.ResponseCachePort;
import io.antcamp.assistantservice.domain.model.SourceReference;
import io.antcamp.assistantservice.infrastructure.config.ResponseCacheConfig;
import io.antcamp.assistantservice.infrastructure.util.JsonConverter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseCacheAdapter implements ResponseCachePort {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingModel embeddingModel;
    private final ResponseCacheConfig config;

    @PostConstruct
    public void ensureTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS p_response_cache (
                    response_cache_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    question_text     TEXT        NOT NULL,
                    question_embedding vector(1536) NOT NULL,
                    answer            TEXT        NOT NULL,
                    sources           JSONB       NOT NULL DEFAULT '[]',
                    expires_at        TIMESTAMP   NOT NULL,
                    created_at        TIMESTAMP   NOT NULL DEFAULT NOW()
                )
                """);
    }

    @Override
    public Optional<CachedEntry> findSimilar(String question) {
        String vectorStr = toVectorString(embeddingModel.embed(question));
        // 만료 x + 유사도 95% 이상 + 가장 유사한 순
        // 0 : 캐시 미스, 1: 캐시 히트
        List<CachedEntry> results = jdbcTemplate.query(
                """
                SELECT answer, sources
                FROM p_response_cache
                WHERE expires_at > NOW()
                  AND 1 - (question_embedding <=> ?::vector) >= ?
                ORDER BY question_embedding <=> ?::vector
                LIMIT 1
                """,
                (rs, rowNum) -> new CachedEntry(
                        rs.getString("answer"),
                        JsonConverter.fromJson(rs.getString("sources"), new TypeReference<List<SourceReference>>() {})
                ),
                vectorStr, config.similarityThreshold(), vectorStr
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public void store(String question, String answer, List<SourceReference> sources) {
        String vectorStr = toVectorString(embeddingModel.embed(question));
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(config.ttlHours());
        // 캐시 일정 주기로 삭제
        jdbcTemplate.update("DELETE FROM p_response_cache WHERE expires_at <= NOW()");
        jdbcTemplate.update(
                """
                INSERT INTO p_response_cache
                    (response_cache_id, question_text, question_embedding, answer, sources, expires_at)
                VALUES (?, ?, ?::vector, ?, ?::jsonb, ?)
                """,
                UUID.randomUUID(), question, vectorStr,
                answer, JsonConverter.toJson(sources), expiresAt
        );
        log.debug("응답 캐시 저장: questionLength={}", question.length());
    }

    private String toVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(embedding[i]);
        }
        return sb.append(']').toString();
    }
}