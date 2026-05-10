package common.dto;

import common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {
    private int status;
    private String code;
    private String message;
    private T data;

    private ApiResponse(int status, String code, String message, T data){
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    //── 성공 응답 (HTTP 200 OK) ──────────────────────────────────

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data){
        return ResponseEntity.ok(
                new ApiResponse<>(200, "SUCCESS", "요청에 성공했습니다.", data)
        );
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS", message, data));
    }

    // 데이터 없이 성공 메시지만 보낼 때 (ex. 로그아웃 성공)
    public static ResponseEntity<ApiResponse<?>> ok(String message) {
        return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS", message, null));
    }

    // ── 생성 응답 (HTTP 201 Created) ──────────────────────────────

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "SUCCESS", message, data));
    }

    // ── 비동기 처리 수락 응답 (HTTP 202 Accepted) ──────────────────

    public static <T> ResponseEntity<ApiResponse<T>> accepted(String message, T data) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(new ApiResponse<>(202, "SUCCESS", message, data));
    }

    // ── 실패 응답 (GlobalExceptionHandler용) ────────────────────

    public static ResponseEntity<ApiResponse<?>> error(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ApiResponse<>(errorCode.getStatus().value(), errorCode.getCode(), errorCode.getMessage(), null));
    }

    public static ResponseEntity<ApiResponse<?>> error(ErrorCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ApiResponse<>(errorCode.getStatus().value(), errorCode.getCode(), message, null));
    }

    // ── Filter용 (ResponseEntity 없이 객체만 반환) ────────────────────
    public static ApiResponse<?> errorBody(ErrorCode errorCode) {
        return new ApiResponse<>(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
    }
}
