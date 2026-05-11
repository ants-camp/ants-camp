package common.dto;

import common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonResponse<T> {
    private int status;
    private String code;
    private String message;
    private T data;

    private CommonResponse(int status, String code, String message, T data){
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    //── 성공 응답 (HTTP 200 OK) ──────────────────────────────────

    public static <T> ResponseEntity<CommonResponse<T>> ok(T data){
        return ResponseEntity.ok(
                new CommonResponse<>(200, "SUCCESS", "요청에 성공했습니다.", data)
        );
    }

    public static <T> ResponseEntity<CommonResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(new CommonResponse<>(200, "SUCCESS", message, data));
    }

    // 데이터 없이 성공 메시지만 보낼 때 (ex. 로그아웃 성공)
    public static ResponseEntity<CommonResponse<?>> ok(String message) {
        return ResponseEntity.ok(new CommonResponse<>(200, "SUCCESS", message, null));
    }

    // ── 생성 응답 (HTTP 201 Created) ──────────────────────────────

    public static <T> ResponseEntity<CommonResponse<T>> created(String message, T data) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new CommonResponse<>(201, "SUCCESS", message, data));
    }

    // ── 실패 응답 (GlobalExceptionHandler용) ────────────────────

    public static ResponseEntity<CommonResponse<?>> error(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new CommonResponse<>(errorCode.getStatus().value(), errorCode.getCode(), errorCode.getMessage(), null));
    }

    public static ResponseEntity<CommonResponse<?>> error(ErrorCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new CommonResponse<>(errorCode.getStatus().value(), errorCode.getCode(), message, null));
    }

    // ── Filter용 (ResponseEntity 없이 객체만 반환) ────────────────────
    public static CommonResponse<?> errorBody(ErrorCode errorCode) {
        return new CommonResponse<>(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
    }
}
