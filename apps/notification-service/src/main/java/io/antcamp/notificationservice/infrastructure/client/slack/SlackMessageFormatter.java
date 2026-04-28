package io.antcamp.notificationservice.infrastructure.client.slack;

import io.antcamp.notificationservice.application.port.ActionResult;
import io.antcamp.notificationservice.domain.model.ResolutionAction;

public class SlackMessageFormatter {

    private SlackMessageFormatter() {}

    public static String threadReplyText(ResolutionAction action, String slackUserId, ActionResult result) {
        if (result instanceof ActionResult.Failure failure) {
            return switch (action) {
                case ROLLBACK -> failure.reason() == ActionResult.FailureReason.NOT_CONFIGURED
                        ? "⚠️ *롤백 이미지가 설정되지 않았습니다* (<@" + slackUserId + ">)\n`ROLLBACK_IMAGE_{서비스명}` 환경변수를 설정하세요."
                        : "❌ *코드 롤백에 실패했습니다* (<@" + slackUserId + ">)\n로그를 확인하고 수동으로 롤백을 진행하세요.";
                case CACHE_CLEAR ->
                        "❌ *캐시 비우기에 실패했습니다* (<@" + slackUserId + ">)\n로그를 확인하고 수동으로 캐시를 초기화하세요.";
                case RESTART ->
                        "❌ *서비스 재시작에 실패했습니다* (<@" + slackUserId + ">)\n로그를 확인하고 수동으로 재시작하세요.";
                case FALSE_ALARM -> "";
            };
        }
        return switch (action) {
            case ROLLBACK ->
                    "🔄 코드 롤백을 실행했습니다 (<@" + slackUserId + ">)\n" +
                    "롤백 완료 후 헬스체크 및 에러율 정상화 여부를 확인하세요.";
            case CACHE_CLEAR ->
                    "🗑️ 캐시 비우기를 실행했습니다 (<@" + slackUserId + ">)\n" +
                    "캐시가 초기화되었습니다. 서비스 응답 정상화 여부를 확인하세요.";
            case RESTART ->
                    "🔁 서비스 재시작을 실행했습니다 (<@" + slackUserId + ">)\n" +
                    "재시작 후 헬스체크 및 에러율 정상화 여부를 확인하세요.";
            case FALSE_ALARM ->
                    "✅ 이 알림은 정상 처리되었습니다 (<@" + slackUserId + ">)";
        };
    }
}