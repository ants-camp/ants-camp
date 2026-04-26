package io.antcamp.notificationservice.infrastructure.client.slack;

import io.antcamp.notificationservice.domain.model.ResolutionAction;

public class SlackMessageFormatter {

    private SlackMessageFormatter() {}

    public static String threadReplyText(ResolutionAction action, String slackUserId) {
        return switch (action) {
            case ROLLBACK ->
                    "🔄 *코드 롤백 가이드* (<@" + slackUserId + ">)\n" +
                    "1. 최근 배포 이력 확인 (CI/CD 파이프라인, git log)\n" +
                    "2. 이전 버전으로 롤백 (git revert 또는 이전 이미지 재배포)\n" +
                    "3. 배포 후 헬스체크 및 에러율 정상화 확인";
            case CACHE_CLEAR ->
                    "🗑️ *캐시 비우기를 실행했습니다* (<@" + slackUserId + ">)\n" +
                    "캐시가 초기화되었습니다. 서비스 응답 정상화 여부를 확인하세요.";
            case RESTART ->
                    "🔁 *서비스 재시작 가이드* (<@" + slackUserId + ">)\n" +
                    "1. 해당 서비스 컨테이너/파드 재시작\n" +
                    "2. 재시작 후 헬스체크 확인 (`/actuator/health`)\n" +
                    "3. 에러율 및 응답시간 정상화 여부 모니터링";
            case FALSE_ALARM ->
                    "✅ *이 알림은 정상 처리되었습니다* (<@" + slackUserId + ">)";
        };
    }
}
