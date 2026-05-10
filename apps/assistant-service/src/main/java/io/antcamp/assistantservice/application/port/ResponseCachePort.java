package io.antcamp.assistantservice.application.port;

import io.antcamp.assistantservice.domain.model.SourceReference;

import java.util.List;
import java.util.Optional;

public interface ResponseCachePort {

    Optional<CachedEntry> findSimilar(String question);

    void store(String question, String answer, List<SourceReference> sources);

    record CachedEntry(String answer, List<SourceReference> sources) {}
}