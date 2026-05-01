package io.antcamp.assistantservice.infrastructure.llm;

import java.util.Arrays;

// Judge LLM 호출 대상 프로바이더 — 모델명 prefix 기반으로 결정
enum Provider {
    OPENAI("gpt"),
    ANTHROPIC("claude"),
    GEMINI("gemini");

    private final String prefix;

    Provider(String prefix) {
        this.prefix = prefix;
    }

    static Provider from(String modelName) {
        String lower = modelName.toLowerCase();
        return Arrays.stream(values())
                .filter(p -> lower.startsWith(p.prefix))
                .findFirst()
                .orElse(OPENAI);
    }
}