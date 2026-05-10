package io.antcamp.assistantservice.infrastructure.security;

import io.antcamp.assistantservice.domain.exception.ForbiddenAccessException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ManagerRoleGuard {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "MANAGER");

    public void require(String role) {
        if (role == null || !ALLOWED_ROLES.contains(role.toUpperCase())) {
            throw new ForbiddenAccessException();
        }
    }
}