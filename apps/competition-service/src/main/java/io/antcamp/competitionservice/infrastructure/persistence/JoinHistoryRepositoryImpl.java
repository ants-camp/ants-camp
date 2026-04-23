package io.antcamp.competitionservice.infrastructure.persistence;

import io.antcamp.competitionservice.domain.repository.JoinHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JoinHistoryRepositoryImpl implements JoinHistoryRepository {
    private final JoinHistoryJpaRepository joinHistoryJpaRepository;

    
}
