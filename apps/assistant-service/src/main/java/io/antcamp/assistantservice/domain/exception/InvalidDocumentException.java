package io.antcamp.assistantservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class InvalidDocumentException extends BusinessException {

    public InvalidDocumentException(ErrorCode errorCode) {
        super(errorCode);
    }
}