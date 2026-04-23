package io.antcamp.competitionservice.infrastructure.persistence;

import io.antcamp.competitionservice.infrastructure.entity.JoinHistoryEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JoinHistoryJpaRepository extends JpaRepository<JoinHistoryEntity, UUID> {
}
