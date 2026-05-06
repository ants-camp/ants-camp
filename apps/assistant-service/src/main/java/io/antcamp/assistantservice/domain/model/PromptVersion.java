package io.antcamp.assistantservice.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class PromptVersion {

    private UUID promptVersionId;
    private String name;    // 예: "v1", "컨텍스트길이증가"
    private String content; // 실제 시스템 프롬프트 템플릿 (%s 자리에 청크가 들어감)

    public static PromptVersion create(String name, String content) {
        return PromptVersion.builder()
                .promptVersionId(UUID.randomUUID())
                .name(name)
                .content(content)
                .build();
    }

    public static PromptVersion restore(UUID promptVersionId, String name, String content) {
        return PromptVersion.builder()
                .promptVersionId(promptVersionId)
                .name(name)
                .content(content)
                .build();
    }
}