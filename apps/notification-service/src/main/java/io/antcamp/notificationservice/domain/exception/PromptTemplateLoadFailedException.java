package io.antcamp.notificationservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class PromptTemplateLoadFailedException extends BusinessException {
    public PromptTemplateLoadFailedException() {
        super(ErrorCode.PROMPT_TEMPLATE_LOAD_FAILED);
    }
}
