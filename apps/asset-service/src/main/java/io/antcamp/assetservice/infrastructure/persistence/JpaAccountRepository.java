package io.antcamp.assetservice.infrastructure.persistence;

import io.antcamp.assetservice.infrastructure.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaAccountRepository extends JpaRepository<AccountEntity, UUID> {
}
