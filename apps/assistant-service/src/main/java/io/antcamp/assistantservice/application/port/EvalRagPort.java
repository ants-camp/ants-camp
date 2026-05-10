package io.antcamp.assistantservice.application.port;

public interface EvalRagPort {

    record EvalRagResult(String content, String modelName, int promptTokens, int completionTokens) {}

    EvalRagResult generate(String ragModel, String systemPrompt, String question);
}