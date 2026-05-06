package io.antcamp.assistantservice.domain.model;

// 평가 질문 — referenceAnswer가 있으면 Reference-based, 없으면 Reference-free 채점
public record EvalQuestion(String question, String referenceAnswer) {
}