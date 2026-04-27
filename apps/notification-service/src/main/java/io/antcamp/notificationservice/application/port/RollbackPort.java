package io.antcamp.notificationservice.application.port;

public interface RollbackPort {
    void rollback(String job);
}
