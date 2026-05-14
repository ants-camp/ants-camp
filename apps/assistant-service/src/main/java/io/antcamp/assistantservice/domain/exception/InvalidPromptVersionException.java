package io.antcamp.assistantservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class InvalidPromptVersionException extends BusinessException {

    private InvalidPromptVersionException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static InvalidPromptVersionException nameBlank() {
        return new InvalidPromptVersionException(ErrorCode.PROMPT_VERSION_NAME_BLANK);
    }

    public static InvalidPromptVersionException contentBlank() {
        return new InvalidPromptVersionException(ErrorCode.PROMPT_VERSION_CONTENT_BLANK);
    }
}