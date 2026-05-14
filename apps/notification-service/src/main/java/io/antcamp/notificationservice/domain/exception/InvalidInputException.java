package io.antcamp.notificationservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class InvalidInputException extends BusinessException {
    public InvalidInputException() {
        super(ErrorCode.INVALID_INPUT);
    }
}
