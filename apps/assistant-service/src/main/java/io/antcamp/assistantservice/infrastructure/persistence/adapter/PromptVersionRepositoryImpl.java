package io.antcamp.assistantservice.infrastructure.persistence.adapter;

import io.antcamp.assistantservice.domain.model.PromptVersion;
import io.antcamp.assistantservice.domain.repository.PromptVersionRepository;
import io.antcamp.assistantservice.infrastructure.entity.PromptVersionEntity;
import io.antcamp.assistantservice.infrastructure.persistence.jpa.JpaPromptVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PromptVersionRepositoryImpl implements PromptVersionRepository {

    private final JpaPromptVersionRepository jpaRepository;

    @Override
    public PromptVersion save(PromptVersion promptVersion) {
        return jpaRepository.save(PromptVersionEntity.from(promptVersion)).toDomain();
    }

    @Override
    public Optional<PromptVersion> findById(UUID promptVersionId) {
        return jpaRepository.findById(promptVersionId).map(PromptVersionEntity::toDomain);
    }

    @Override
    public List<PromptVersion> findAll() {
        return jpaRepository.findAll().stream().map(PromptVersionEntity::toDomain).toList();
    }
}