package io.antcamp.assistantservice.infrastructure.security;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PlayerRoleGuard {

    private static final Set<String> ALLOWED_ROLES = Set.of("MANAGER", "MASTER", "PLAYER");

    public void require(String role) {
        if (role == null || !ALLOWED_ROLES.contains(role.toUpperCase())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}