package io.antcamp.assistantservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class MessageTooLongException extends BusinessException {

    public MessageTooLongException() {
        super(ErrorCode.MESSAGE_TOO_LONG);
    }
}