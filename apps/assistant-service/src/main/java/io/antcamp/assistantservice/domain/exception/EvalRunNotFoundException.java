package io.antcamp.assistantservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class EvalRunNotFoundException extends BusinessException {

    public EvalRunNotFoundException() {
        super(ErrorCode.EVAL_RUN_NOT_FOUND);
    }
}