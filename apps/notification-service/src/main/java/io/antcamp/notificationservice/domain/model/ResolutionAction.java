package io.antcamp.notificationservice.domain.model;

public enum ResolutionAction {

    ROLLBACK("코드 롤백"),
    CACHE_CLEAR("캐시 비우기"),
    RESTART("서비스 재시작"),
    FALSE_ALARM("정상 처리");

    private final String displayName;

    ResolutionAction(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}