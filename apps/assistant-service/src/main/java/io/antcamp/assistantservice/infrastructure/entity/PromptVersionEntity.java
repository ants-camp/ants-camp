package io.antcamp.assistantservice.infrastructure.entity;

import common.entity.BaseEntity;
import io.antcamp.assistantservice.domain.model.PromptVersion;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "p_prompt_versions")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromptVersionEntity extends BaseEntity {

    @Id
    @Column(name = "prompt_version_id", updatable = false, nullable = false)
    private UUID promptVersionId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    public static PromptVersionEntity from(PromptVersion domain) {
        return PromptVersionEntity.builder()
                .promptVersionId(domain.getPromptVersionId())
                .name(domain.getName())
                .content(domain.getContent())
                .build();
    }

    public PromptVersion toDomain() {
        return PromptVersion.restore(this.promptVersionId, this.name, this.content);
    }
}