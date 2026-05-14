package io.antcamp.assistantservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class InvalidMessageContentException extends BusinessException {

    public InvalidMessageContentException() {
        super(ErrorCode.INVALID_MESSAGE_CONTENT);
    }
}