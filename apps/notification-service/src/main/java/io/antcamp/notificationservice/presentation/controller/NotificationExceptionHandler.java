package io.antcamp.notificationservice.presentation.controller;

import io.antcamp.notificationservice.domain.exception.AlreadyHandledException;
import io.antcamp.notificationservice.domain.exception.ContainerNotFoundException;
import io.antcamp.notificationservice.domain.exception.InfrastructureServiceException;
import io.antcamp.notificationservice.domain.exception.NotificationNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class NotificationExceptionHandler {

    // 알림 찾을 수 없음
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotificationNotFound(NotificationNotFoundException e) {
        log.warn("[NotificationNotFoundException] message={}", e.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND", e.getMessage());
    }

    // 이미 처리된 알림
    @ExceptionHandler(AlreadyHandledException.class)
    public ResponseEntity<Map<String, String>> handleAlreadyHandled(AlreadyHandledException e) {
        log.warn("[AlreadyHandledException] message={}", e.getMessage());
        return createErrorResponse(HttpStatus.CONFLICT, "ALREADY_HANDLED", e.getMessage());
    }

    // 컨테이너 찾을 수 없음
    @ExceptionHandler(ContainerNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleContainerNotFound(ContainerNotFoundException e) {
        log.warn("[ContainerNotFoundException] message={}", e.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "CONTAINER_NOT_FOUND", e.getMessage());
    }

    // 인프라 서비스 조작 시도
    @ExceptionHandler(InfrastructureServiceException.class)
    public ResponseEntity<Map<String, String>> handleInfrastructureService(InfrastructureServiceException e) {
        log.warn("[InfrastructureServiceException] message={}", e.getMessage());
        return createErrorResponse(HttpStatus.FORBIDDEN, "INFRASTRUCTURE_SERVICE_FORBIDDEN", e.getMessage());
    }

    // @Valid 유효성 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("입력값이 유효하지 않습니다.");
        log.warn("[ValidationException] message={}", message);
        return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_INPUT", message);
    }

    // JSON 파싱 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("[HttpMessageNotReadableException] message={}", e.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "요청 형식이 올바르지 않습니다.");
    }

    // 예상치 못한 서버 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnhandledException(Exception e) {
        log.error("[UnhandledException]", e);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");
    }

    private ResponseEntity<Map<String, String>> createErrorResponse(HttpStatus status, String code, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("code", code);
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}