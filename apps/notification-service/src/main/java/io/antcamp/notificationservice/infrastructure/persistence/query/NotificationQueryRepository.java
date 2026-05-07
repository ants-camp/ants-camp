package io.antcamp.notificationservice.infrastructure.persistence.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.antcamp.notificationservice.domain.repository.NotificationSearchCriteria;
import io.antcamp.notificationservice.infrastructure.entity.NotificationEntity;
import io.antcamp.notificationservice.infrastructure.entity.QNotificationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<NotificationEntity> search(NotificationSearchCriteria criteria, Pageable pageable) {
        QNotificationEntity n = QNotificationEntity.notificationEntity;

        BooleanExpression[] conditions = {
                criteria.status() != null ? n.status.eq(criteria.status()) : null,
                criteria.severity() != null ? n.severity.eq(criteria.severity()) : null,
                criteria.source() != null ? n.source.eq(criteria.source()) : null,
                criteria.job() != null && !criteria.job().isBlank() ? n.job.eq(criteria.job()) : null,
                criteria.from() != null ? n.createdAt.goe(criteria.from()) : null,
                criteria.to() != null ? n.createdAt.loe(criteria.to()) : null,
                criteria.actionUserEmail() != null ? n.actionUserEmail.eq(criteria.actionUserEmail()) : null,
                Boolean.TRUE.equals(criteria.handledOnly()) ? n.resolutionAction.isNotNull() : null
        };

        List<NotificationEntity> content = queryFactory
                .selectFrom(n)
                .where(conditions)
                .orderBy(n.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory.select(n.count()).from(n).where(conditions).fetchOne()
        ).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }
}
