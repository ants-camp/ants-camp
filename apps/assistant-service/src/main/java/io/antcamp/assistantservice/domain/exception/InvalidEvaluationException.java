package io.antcamp.assistantservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class InvalidEvaluationException extends BusinessException {

    private InvalidEvaluationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static InvalidEvaluationException questionsEmpty() {
        return new InvalidEvaluationException(ErrorCode.EVAL_QUESTIONS_EMPTY);
    }

    public static InvalidEvaluationException judgeModelsEmpty() {
        return new InvalidEvaluationException(ErrorCode.EVAL_JUDGE_MODELS_EMPTY);
    }

    public static InvalidEvaluationException tooManyCombinations(int questions, int judges, int max) {
        throw new InvalidEvaluationException(ErrorCode.EVAL_TOO_MANY_COMBINATIONS);
    }
}