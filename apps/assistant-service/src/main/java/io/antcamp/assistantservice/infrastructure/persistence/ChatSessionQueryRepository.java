package io.antcamp.assistantservice.infrastructure.persistence;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.antcamp.assistantservice.domain.model.CursorSlice;
import io.antcamp.assistantservice.infrastructure.entity.ChatSessionEntity;
import io.antcamp.assistantservice.infrastructure.entity.QChatSessionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ChatSessionQueryRepository {

    private final JPAQueryFactory queryFactory;

    public CursorSlice<ChatSessionEntity, LocalDateTime> findSessions(
            UUID userId, String keyword, LocalDateTime lastUpdatedAt, int pageSize) {
        QChatSessionEntity session = QChatSessionEntity.chatSessionEntity;

        List<ChatSessionEntity> rows = queryFactory
                .selectFrom(session)
                .where(
                        session.userId.eq(userId),
                        keywordContains(session, keyword),
                        cursorBefore(session, lastUpdatedAt)
                )
                .orderBy(session.updatedAt.desc())
                .limit(pageSize + 1L)
                .fetch();

        boolean hasNext = rows.size() > pageSize;
        List<ChatSessionEntity> page = hasNext ? rows.subList(0, pageSize) : rows;
        LocalDateTime nextCursor = page.isEmpty() ? null : page.get(page.size() - 1).getUpdatedAt();

        return new CursorSlice<>(page, hasNext, nextCursor);
    }

    private BooleanExpression keywordContains(QChatSessionEntity session, String keyword) {
        return (keyword != null && !keyword.isBlank()) ? session.title.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression cursorBefore(QChatSessionEntity session, LocalDateTime lastUpdatedAt) {
        return lastUpdatedAt != null ? session.updatedAt.lt(lastUpdatedAt) : null;
    }
}