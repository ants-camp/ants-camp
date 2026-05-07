package io.antcamp.assistantservice.presentation.dto.response;

import io.antcamp.assistantservice.domain.model.EvalQuestion;

import java.util.List;

public record GeneratedQuestionsResponse(List<EvalQuestionResponse> questions) {

    public record EvalQuestionResponse(String question, String referenceAnswer) {}

    public static GeneratedQuestionsResponse from(List<EvalQuestion> questions) {
        return new GeneratedQuestionsResponse(
                questions.stream()
                        .map(q -> new EvalQuestionResponse(q.question(), q.referenceAnswer()))
                        .toList()
        );
    }
}