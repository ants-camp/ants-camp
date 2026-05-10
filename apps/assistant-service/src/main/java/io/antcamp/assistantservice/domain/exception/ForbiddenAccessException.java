package io.antcamp.assistantservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class ForbiddenAccessException extends BusinessException {

    public ForbiddenAccessException() {
        super(ErrorCode.FORBIDDEN);
    }
}