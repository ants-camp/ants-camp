package io.antcamp.assistantservice.domain.model;

// p_eval_results.scores JSONB
public record EvalScores(double relevance, double faithfulness, double contextPrecision, String feedback) {
}