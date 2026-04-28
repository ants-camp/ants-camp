package io.antcamp.assistantservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class SessionNotFoundException extends BusinessException {

    public SessionNotFoundException() {
        super(ErrorCode.INVALID_INPUT);
    }
}