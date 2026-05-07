package io.antcamp.notificationservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class NotificationException extends BusinessException {

    private NotificationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static NotificationException notFound() {
        return new NotificationException(ErrorCode.NOTIFICATION_NOT_FOUND);
    }

    public static NotificationException invalidState() {
        return new NotificationException(ErrorCode.NOTIFICATION_INVALID_STATE);
    }

    public static NotificationException invalidField() {
        return new NotificationException(ErrorCode.NOTIFICATION_INVALID_FIELD);
    }

    public static NotificationException alreadyHandled() {
        return new NotificationException(ErrorCode.NOTIFICATION_ALREADY_HANDLED);
    }
}
