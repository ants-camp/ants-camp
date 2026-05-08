package common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── Auth / Token ──────────────────────────────────
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "만료된 토큰입니다. 다시 로그인해 주세요."),
    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "TOKEN_BLACKLISTED", "이미 로그아웃된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),
    TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "TOKEN_MISMATCH", "토큰 정보가 일치하지 않습니다. 다시 로그인해주세요."),

    // ── User ──────────────────────────────────────────
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 사용자입니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "DUPLICATE_USERNAME", "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD", "비밀번호가 일치하지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    ALREADY_DELETED(HttpStatus.CONFLICT, "ALREADY_DELETED", "이미 탈퇴한 사용자입니다."),
    WITHDRAWN_USER(HttpStatus.BAD_REQUEST, "WITHDRAWN_USER", "탈퇴한 계정입니다."),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "INVALID_ROLE", "유효하지 않은 권한 값입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "LOGIN_FAILED", "아이디 또는 비밀번호가 올바르지 않습니다."),
    CANNOT_DELETE_SELF(HttpStatus.FORBIDDEN, "CANNOT_DELETE_SELF", "본인 계정은 삭제할 수 없습니다."),
    CANNOT_UPDATE_ROLE_SELF(HttpStatus.FORBIDDEN, "CANNOT_UPDATE_ROLE_SELF", "본인 권한은 변경할 수 없습니다."),
    USER_NOT_APPROVED(HttpStatus.FORBIDDEN, "USER_NOT_APPROVED", "승인되지 않은 사용자입니다."),
    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "SAME_AS_OLD_PASSWORD", "새 비밀번호는 현재 비밀번호와 달라야 합니다."),

    // ── Competition ───────────────────────────────────
    COMPETITION_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPETITION_NOT_FOUND", "존재하지 않는 대회입니다."),

    // ── Assistant ─────────────────────────────────────
    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND", "존재하지 않는 문서입니다."),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "존재하지 않는 세션입니다."),
    INVALID_MESSAGE_CONTENT(HttpStatus.BAD_REQUEST, "INVALID_MESSAGE_CONTENT", "메시지 내용은 비어있을 수 없습니다."),
    MESSAGE_TOO_LONG(HttpStatus.BAD_REQUEST, "MESSAGE_TOO_LONG", "메시지는 2000자를 초과할 수 없습니다."),
    DOCUMENT_TITLE_BLANK(HttpStatus.BAD_REQUEST, "DOCUMENT_TITLE_BLANK", "문서 제목은 비어있을 수 없습니다."),
    DOCUMENT_TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "DOCUMENT_TITLE_TOO_LONG", "문서 제목은 100자를 초과할 수 없습니다."),
    DOCUMENT_CONTENT_BLANK(HttpStatus.BAD_REQUEST, "DOCUMENT_CONTENT_BLANK", "문서 내용은 비어있을 수 없습니다."),
    EVAL_QUESTIONS_EMPTY(HttpStatus.BAD_REQUEST, "EVAL_QUESTIONS_EMPTY", "평가 질문은 최소 1개 이상이어야 합니다."),
    EVAL_JUDGE_MODELS_EMPTY(HttpStatus.BAD_REQUEST, "EVAL_JUDGE_MODELS_EMPTY", "평가 모델은 최소 1개 이상이어야 합니다."),
    EVAL_TOO_MANY_COMBINATIONS(HttpStatus.BAD_REQUEST, "EVAL_TOO_MANY_COMBINATIONS", "질문 × 모델 조합이 허용 한도를 초과했습니다."),
    EVAL_RUN_NOT_FOUND(HttpStatus.NOT_FOUND, "EVAL_RUN_NOT_FOUND", "존재하지 않는 평가 실행입니다."),

    // ── Common ────────────────────────────────────────
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "입력값이 유효하지 않습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."),

    // ── Trade ────────────────────────────────────────
    TRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "TRADE_NOT_FOUND", "존재하지 않는 매매입니다."),
    TRADE_ALREADY_PROCESSED(HttpStatus.NOT_FOUND, "TRADE_ALREADY_PROCESSED", "이미 처리된 주문 취소 시도 (PENDING이 아닐 때)입니다."), // 이미 처리된 주문 취소 시도 (PENDING이 아닐 때)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "요청 파라미터 오류입니다."),     // 요청 파라미터 오류

    // ── Asset ────────────────────────────────────────
    ASSET_SERVICE_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "ASSET_SERVICE_ERROR", "자산 서비스 에러입니다."),

    // ── KIS ────────────────────────────────────────
    KIS_SERVER_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "KIS_SERVICE_ERROR", "KIS 서비스 에러입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
