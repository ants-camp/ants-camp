package io.antcamp.notificationservice.application.port;

import io.antcamp.notificationservice.domain.model.Notification;
import io.antcamp.notificationservice.domain.model.ResolutionAction;

public interface AlertPort {
    String send(Notification notification);
    String getUserEmail(String slackUserId);
    void postThreadReply(String channelId, String threadTs, String text);
    void markAsHandled(Notification notification, String handlerSlackUserId, ResolutionAction action);
}