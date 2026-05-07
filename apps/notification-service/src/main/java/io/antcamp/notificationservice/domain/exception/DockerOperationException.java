package io.antcamp.notificationservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class DockerOperationException extends BusinessException {

    private DockerOperationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static DockerOperationException containerNotFound() {
        return new DockerOperationException(ErrorCode.CONTAINER_NOT_FOUND);
    }

    public static DockerOperationException operationForbidden() {
        return new DockerOperationException(ErrorCode.INFRASTRUCTURE_OPERATION_FORBIDDEN);
    }

    public static DockerOperationException rollbackImageNotConfigured() {
        return new DockerOperationException(ErrorCode.ROLLBACK_IMAGE_NOT_CONFIGURED);
    }

    public static DockerOperationException operationFailed() {
        return new DockerOperationException(ErrorCode.DOCKER_OPERATION_FAILED);
    }
}
