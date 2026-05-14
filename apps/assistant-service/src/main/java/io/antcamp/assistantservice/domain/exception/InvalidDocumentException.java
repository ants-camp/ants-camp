package io.antcamp.assistantservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class InvalidDocumentException extends BusinessException {

    private InvalidDocumentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static InvalidDocumentException titleBlank() {
        return new InvalidDocumentException(ErrorCode.DOCUMENT_TITLE_BLANK);
    }

    public static InvalidDocumentException titleTooLong() {
        return new InvalidDocumentException(ErrorCode.DOCUMENT_TITLE_TOO_LONG);
    }

    public static InvalidDocumentException contentBlank() {
        return new InvalidDocumentException(ErrorCode.DOCUMENT_CONTENT_BLANK);
    }

    public static InvalidDocumentException typeNull() {
        return new InvalidDocumentException(ErrorCode.DOCUMENT_TYPE_NULL);
    }
}