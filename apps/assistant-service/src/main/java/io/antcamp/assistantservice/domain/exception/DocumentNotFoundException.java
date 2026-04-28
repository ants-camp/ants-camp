package io.antcamp.assistantservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class DocumentNotFoundException extends BusinessException {

    public DocumentNotFoundException() {
        super(ErrorCode.INVALID_INPUT);
    }
}