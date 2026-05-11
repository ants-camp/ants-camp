package io.antcamp.notificationservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class SlackApiException extends BusinessException {
    public SlackApiException() {
        super(ErrorCode.SLACK_API_ERROR);
    }
}
