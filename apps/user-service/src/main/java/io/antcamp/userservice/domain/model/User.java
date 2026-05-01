package io.antcamp.userservice.domain.model;

import common.entity.BaseEntity;
import io.antcamp.userservice.domain.model.enums.RoleType;
import io.antcamp.userservice.domain.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
@Table(name = "p_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String email; // 로그인 ID

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
}

