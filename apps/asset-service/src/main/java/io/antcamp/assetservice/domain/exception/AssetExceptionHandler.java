package io.antcamp.assetservice.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class AssetExceptionHandler {
    //계좌 확인 불가
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAccountNotFoundException(AccountNotFoundException e) {
        return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }
    //입력 금액 잘못됨
    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<Map<String, String>> handleInvalidAmountException(InvalidAmountException e) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    //잔액 부족
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientBalanceException(InsufficientBalanceException e) {
        return createErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    //남의 계좌 접근 차단
    @ExceptionHandler(UnauthorizedAccountAccessException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedAccountAccessException(UnauthorizedAccountAccessException e) {
        return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    // 보유 주식 확인 불가
    @ExceptionHandler(HoldingNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleHoldingNotFoundException(HoldingNotFoundException e) {
        return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    //공통 에러 응답
    private ResponseEntity<Map<String, String>> createErrorResponse(HttpStatus status, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}
