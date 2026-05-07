package io.antcamp.assistantservice.domain.repository;

import io.antcamp.assistantservice.domain.model.PromptVersion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromptVersionRepository {

    PromptVersion save(PromptVersion promptVersion);

    Optional<PromptVersion> findById(UUID promptVersionId);

    List<PromptVersion> findAll();
}