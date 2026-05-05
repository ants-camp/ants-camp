package io.antcamp.notificationservice.application.port;

import io.antcamp.notificationservice.domain.model.Notification;
import io.antcamp.notificationservice.domain.model.ResolutionAction;

public interface AlertPort {
    String send(Notification notification);
    String getUserEmail(String slackUserId);
    void notifyActionResult(String channelId, String threadTs, ResolutionAction action, String slackUserId, ActionResult result);
    void markAsProcessing(Notification notification, String handlerSlackUserId, ResolutionAction action);
    void markAsHandled(Notification notification, String handlerSlackUserId, ResolutionAction action, boolean succeeded);
}