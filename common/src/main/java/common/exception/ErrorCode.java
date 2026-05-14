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
    COMPETITION_PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPETITION_PARTICIPANT_NOT_FOUND", "대회 참가 신청 내역이 없습니다."),
    COMPETITION_ALREADY_REGISTERED(HttpStatus.CONFLICT, "COMPETITION_ALREADY_REGISTERED", "이미 신청한 대회입니다."),
    COMPETITION_NOT_REGISTERABLE(HttpStatus.BAD_REQUEST, "COMPETITION_NOT_REGISTERABLE", "현재 신청할 수 없는 대회입니다."),
    COMPETITION_INVALID_STATUS(HttpStatus.CONFLICT, "COMPETITION_INVALID_STATUS", "현재 대회 상태에서 허용되지 않는 작업입니다."),
    COMPETITION_MIN_PARTICIPANTS_NOT_MET(HttpStatus.BAD_REQUEST, "COMPETITION_MIN_PARTICIPANTS_NOT_MET",
            "최소 참가자 수를 충족하지 못해 대회를 시작할 수 없습니다."),
    COMPETITION_ALREADY_FINISHED(HttpStatus.CONFLICT, "COMPETITION_ALREADY_FINISHED", "이미 종료된 대회입니다."),
    COMPETITION_ALREADY_PUBLISHED(HttpStatus.CONFLICT, "COMPETITION_ALREADY_PUBLISHED", "이미 공개된 대회입니다."),
    COMPETITION_CANNOT_UPDATE(HttpStatus.CONFLICT, "COMPETITION_CANNOT_UPDATE", "종료되었거나 취소된 대회는 수정할 수 없습니다."),
    COMPETITION_CANNOT_START(HttpStatus.CONFLICT, "COMPETITION_CANNOT_START", "준비 중인 대회만 시작할 수 있습니다."),
    COMPETITION_CANNOT_FINISH(HttpStatus.CONFLICT, "COMPETITION_CANNOT_FINISH", "진행 중인 대회만 종료할 수 있습니다."),
    COMPETITION_CANNOT_CANCEL_REGISTRATION(HttpStatus.CONFLICT, "COMPETITION_CANNOT_CANCEL_REGISTRATION",
            "준비 중인 대회에서만 신청을 취소할 수 있습니다."),
    COMPETITION_CANNOT_PUBLISH(HttpStatus.CONFLICT, "COMPETITION_CANNOT_PUBLISH", "준비 중인 대회만 공개할 수 있습니다."),
    COMPETITION_PARTICIPANT_COUNT_CANNOT_DECREASE(HttpStatus.CONFLICT, "COMPETITION_PARTICIPANT_COUNT_CANNOT_DECREASE",
            "현재 참가자가 없어 취소할 수 없습니다."),
    COMPETITION_REGISTER_PERIOD_START_AFTER_END(HttpStatus.BAD_REQUEST, "COMPETITION_REGISTER_PERIOD_START_AFTER_END",
            "신청 시작일은 신청 종료일보다 이전이어야 합니다."),
    COMPETITION_REGISTER_START_IN_PAST(HttpStatus.BAD_REQUEST, "COMPETITION_REGISTER_START_IN_PAST",
            "신청 시작일은 현재 시각 이후여야 합니다."),
    COMPETITION_PERIOD_START_AFTER_END(HttpStatus.BAD_REQUEST, "COMPETITION_PERIOD_START_AFTER_END",
            "대회 시작일은 대회 종료일보다 이전이어야 합니다."),
    COMPETITION_PERIOD_START_IN_PAST(HttpStatus.BAD_REQUEST, "COMPETITION_PERIOD_START_IN_PAST",
            "대회 시작일은 현재 시각 이후여야 합니다."),
    COMPETITION_REGISTER_END_AFTER_COMPETITION_START(HttpStatus.BAD_REQUEST,
            "COMPETITION_REGISTER_END_AFTER_COMPETITION_START", "신청 종료일은 대회 시작일보다 이전이어야 합니다."),
    COMPETITION_PARTICIPANT_MIN_EXCEEDS_MAX(HttpStatus.BAD_REQUEST, "COMPETITION_PARTICIPANT_MIN_EXCEEDS_MAX",
            "최소 참가 인원은 최대 참가 인원보다 클 수 없습니다."),
    COMPETITION_PARTICIPANT_COUNT_NEGATIVE(HttpStatus.BAD_REQUEST, "COMPETITION_PARTICIPANT_COUNT_NEGATIVE",
            "참가 인원 수는 0 이상이어야 합니다."),
    COMPETITION_CHANGE_NOTICE_BEFORE_CONTENTS_REQUIRED(HttpStatus.BAD_REQUEST,
            "COMPETITION_CHANGE_NOTICE_BEFORE_CONTENTS_REQUIRED", "변경 전 내용은 필수입니다."),
    COMPETITION_CHANGE_NOTICE_AFTER_CONTENTS_REQUIRED(HttpStatus.BAD_REQUEST,
            "COMPETITION_CHANGE_NOTICE_AFTER_CONTENTS_REQUIRED", "변경 후 내용은 필수입니다."),
    COMPETITION_CHANGE_NOTICE_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "COMPETITION_CHANGE_NOTICE_REASON_REQUIRED",
            "변경 사유는 필수입니다."),
    COMPETITION_CANNOT_REGISTER(HttpStatus.BAD_REQUEST, "COMPETITION_CANNOT_REGISTER", "대회 신청기간이 지나서 신청할 수 없습니다."),

    // ── Ranking ───────────────────────────────────────
    RANKING_NOT_FOUND(HttpStatus.NOT_FOUND, "RANKING_NOT_FOUND", "랭킹 정보를 찾을 수 없습니다."),
    RANKING_ALREADY_FINALIZED(HttpStatus.CONFLICT, "RANKING_ALREADY_FINALIZED", "이미 확정된 랭킹입니다."),

    // ── Assistant ─────────────────────────────────────
    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND", "존재하지 않는 문서입니다."),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "존재하지 않는 세션입니다."),
    INVALID_MESSAGE_CONTENT(HttpStatus.BAD_REQUEST, "INVALID_MESSAGE_CONTENT", "메시지 내용은 비어있을 수 없습니다."),
    MESSAGE_TOO_LONG(HttpStatus.BAD_REQUEST, "MESSAGE_TOO_LONG", "메시지는 2000자를 초과할 수 없습니다."),
    DOCUMENT_TITLE_BLANK(HttpStatus.BAD_REQUEST, "DOCUMENT_TITLE_BLANK", "문서 제목은 비어있을 수 없습니다."),
    DOCUMENT_TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "DOCUMENT_TITLE_TOO_LONG", "문서 제목은 100자를 초과할 수 없습니다."),
    DOCUMENT_CONTENT_BLANK(HttpStatus.BAD_REQUEST, "DOCUMENT_CONTENT_BLANK", "문서 내용은 비어있을 수 없습니다."),
    DOCUMENT_TYPE_NULL(HttpStatus.BAD_REQUEST, "DOCUMENT_TYPE_NULL", "문서 타입은 필수입니다."),
    EVAL_QUESTIONS_EMPTY(HttpStatus.BAD_REQUEST, "EVAL_QUESTIONS_EMPTY", "평가 질문은 최소 1개 이상이어야 합니다."),
    EVAL_JUDGE_MODELS_EMPTY(HttpStatus.BAD_REQUEST, "EVAL_JUDGE_MODELS_EMPTY", "평가 모델은 최소 1개 이상이어야 합니다."),
    EVAL_TOO_MANY_COMBINATIONS(HttpStatus.BAD_REQUEST, "EVAL_TOO_MANY_COMBINATIONS", "질문 × 모델 조합이 허용 한도를 초과했습니다."),
    EVAL_RUN_NOT_FOUND(HttpStatus.NOT_FOUND, "EVAL_RUN_NOT_FOUND", "존재하지 않는 평가 실행입니다."),
    EVAL_SAME_RUN_CONFIG(HttpStatus.BAD_REQUEST, "EVAL_SAME_RUN_CONFIG", "모델과 프롬프트 버전이 동일한 Run은 비교할 수 없습니다."),
    PROMPT_VERSION_NAME_BLANK(HttpStatus.BAD_REQUEST, "PROMPT_VERSION_NAME_BLANK", "프롬프트 버전 이름은 비어있을 수 없습니다."),
    PROMPT_VERSION_CONTENT_BLANK(HttpStatus.BAD_REQUEST, "PROMPT_VERSION_CONTENT_BLANK", "프롬프트 내용은 비어있을 수 없습니다."),

    // ── Common ────────────────────────────────────────
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "입력값이 유효하지 않습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."),

    // ── Trade ────────────────────────────────────────
    TRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "TRADE_NOT_FOUND", "존재하지 않는 매매입니다."),
    TRADE_ALREADY_PROCESSED(HttpStatus.NOT_FOUND, "TRADE_ALREADY_PROCESSED",
            "이미 처리된 주문 취소 시도 (PENDING이 아닐 때)입니다."), // 이미 처리된 주문 취소 시도 (PENDING이 아닐 때)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "요청 파라미터 오류입니다."),     // 요청 파라미터 오류
    // 수정 후 신규: 접수 시점 사전 검증 실패용
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE", "현금 잔액이 부족합니다."),
    INSUFFICIENT_HOLDINGS(HttpStatus.BAD_REQUEST, "INSUFFICIENT_HOLDINGS", "매도 가능한 보유 수량이 부족합니다."),

    // ── Asset ────────────────────────────────────────
    ASSET_SERVICE_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "ASSET_SERVICE_ERROR", "자산 서비스 에러입니다."),

    // ── KIS ────────────────────────────────────────
    KIS_SERVER_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "KIS_SERVICE_ERROR", "KIS 서비스 에러입니다."),

    // ── Notification ──────────────────────────────────
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND", "존재하지 않는 알림입니다."),
    NOTIFICATION_ALREADY_HANDLED(HttpStatus.CONFLICT, "NOTIFICATION_ALREADY_HANDLED", "이미 처리된 알림입니다."),
    NOTIFICATION_INVALID_STATE(HttpStatus.CONFLICT, "NOTIFICATION_INVALID_STATE", "알림의 현재 상태에서 수행할 수 없는 작업입니다."),
    NOTIFICATION_INVALID_FIELD(HttpStatus.BAD_REQUEST, "NOTIFICATION_INVALID_FIELD", "알림 필드 값이 유효하지 않습니다."),
    CONTAINER_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTAINER_NOT_FOUND", "대상 컨테이너를 찾을 수 없습니다."),
    INFRASTRUCTURE_OPERATION_FORBIDDEN(HttpStatus.FORBIDDEN, "INFRASTRUCTURE_OPERATION_FORBIDDEN",
            "인프라 서비스는 조작할 수 없습니다."),
    SLACK_API_ERROR(HttpStatus.BAD_GATEWAY, "SLACK_API_ERROR", "Slack API 호출에 실패했습니다."),
    DOCKER_OPERATION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "DOCKER_OPERATION_FAILED", "Docker 작업에 실패했습니다."),
    ROLLBACK_IMAGE_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "ROLLBACK_IMAGE_NOT_CONFIGURED",
            "롤백 이미지가 설정되지 않았습니다."),
    PROMPT_TEMPLATE_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PROMPT_TEMPLATE_LOAD_FAILED",
            "프롬프트 템플릿 로드에 실패했습니다."),
    INVALID_CACHE_PATTERN(HttpStatus.BAD_REQUEST, "INVALID_CACHE_PATTERN", "캐시 키 패턴이 유효하지 않습니다."),
    PROMETHEUS_METRIC_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "PROMETHEUS_METRIC_INVALID",
            "Prometheus 메트릭 값이 유효 범위를 벗어났습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
