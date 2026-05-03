package io.antcamp.notificationservice.application.port;

public sealed interface ActionResult permits ActionResult.Success, ActionResult.Failure {
    
    record Success() implements ActionResult {}

    record Failure(FailureReason reason) implements ActionResult {}

    enum FailureReason {
        NOT_CONFIGURED,
        EXECUTION_ERROR
    }
}