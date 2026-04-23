package common.exception;

import common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 직접 던지는 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(BusinessException e) {
        log.warn("[CustomException] code={}, message={}", e.getErrorCode().getCode(), e.getMessage());
        // 내부 status와 HTTP status를 한 번에 해결
        return ApiResponse.error(e.getErrorCode());
    }

    // @Valid 유효성 검사 실패한 경우 던지는 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorCode.INVALID_INPUT.getMessage());

        log.warn("[ValidationException] message={}", message);

        // ResponseEntity.status().body()를 생략하고 바로 반환
        return ApiResponse.error(ErrorCode.INVALID_INPUT, message);
    }

    // Request JSON 필드 비어있는 경우 던지는 예외
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("[HttpMessageNotReadableException] message={}", e.getMessage());
        return ApiResponse.error(ErrorCode.INVALID_INPUT);
    }

    // 나머지 예상 못한 예외상황
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> unhandledException(Exception e) {
        log.error("[UnhandledException]", e);
        // 여기서도 ApiResponse.error()만 사용
        return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
