package io.antcamp.notificationservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class InvalidCachePatternException extends BusinessException {
    public InvalidCachePatternException() {
        super(ErrorCode.INVALID_CACHE_PATTERN);
    }
}
