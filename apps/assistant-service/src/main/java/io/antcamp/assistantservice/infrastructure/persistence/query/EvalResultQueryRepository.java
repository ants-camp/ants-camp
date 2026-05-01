package io.antcamp.assistantservice.infrastructure.persistence.query;

import com.fasterxml.jackson.core.type.TypeReference;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.antcamp.assistantservice.domain.model.CursorSlice;
import io.antcamp.assistantservice.domain.model.EvalResultView;
import io.antcamp.assistantservice.domain.model.EvalScores;
import io.antcamp.assistantservice.domain.model.RetrievedChunk;
import io.antcamp.assistantservice.infrastructure.entity.QEvalResultEntity;
import io.antcamp.assistantservice.infrastructure.entity.QRagQueryEntity;
import io.antcamp.assistantservice.infrastructure.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EvalResultQueryRepository {

    private final JPAQueryFactory queryFactory;

    public CursorSlice<EvalResultView, LocalDateTime> findResults(
            String judgeModel, LocalDateTime lastUpdatedAt, LocalDate startDate, LocalDate endDate, int pageSize) {

        QEvalResultEntity eval = QEvalResultEntity.evalResultEntity;
        QRagQueryEntity rag = QRagQueryEntity.ragQueryEntity;

        List<Tuple> rows = queryFactory
                .select(
                        eval.evalResultId,
                        eval.ragQueryId,
                        rag.userQuery,
                        rag.llmResponse,
                        eval.judgeModel,
                        eval.scores,
                        eval.createdAt,
                        eval.updatedAt,
                        rag.retrievedChunks,
                        rag.promptUsed
                )
                .from(eval)
                .join(rag).on(eval.ragQueryId.eq(rag.ragQueryId))
                .where(
                        eval.deletedAt.isNull(),
                        judgeModelEq(eval, judgeModel),
                        cursorBefore(eval, lastUpdatedAt),
                        startDateGte(eval, startDate),
                        endDateLt(eval, endDate)
                )
                .orderBy(eval.updatedAt.desc())
                .limit(pageSize + 1L)
                .fetch();

        boolean hasNext = rows.size() > pageSize;
        List<Tuple> page = hasNext ? rows.subList(0, pageSize) : rows;
        LocalDateTime nextCursor = page.isEmpty() ? null : page.get(page.size() - 1).get(eval.updatedAt);

        List<EvalResultView> views = page.stream().map(t -> toView(t, eval, rag)).toList();
        return new CursorSlice<>(views, hasNext, nextCursor);
    }

    private EvalResultView toView(Tuple t, QEvalResultEntity eval, QRagQueryEntity rag) {
        EvalScores scores = JsonConverter.fromJson(t.get(eval.scores), new TypeReference<>() {});
        List<RetrievedChunk> chunks = JsonConverter.fromJson(t.get(rag.retrievedChunks), new TypeReference<>() {});
        return new EvalResultView(
                t.get(eval.evalResultId),
                t.get(eval.ragQueryId),
                t.get(rag.userQuery),
                t.get(rag.llmResponse),
                t.get(eval.judgeModel),
                scores,
                t.get(eval.createdAt),
                chunks,
                t.get(rag.promptUsed)
        );
    }

    private BooleanExpression judgeModelEq(QEvalResultEntity eval, String judgeModel) {
        return (judgeModel != null && !judgeModel.isBlank()) ? eval.judgeModel.eq(judgeModel) : null;
    }

    private BooleanExpression cursorBefore(QEvalResultEntity eval, LocalDateTime lastUpdatedAt) {
        return lastUpdatedAt != null ? eval.updatedAt.lt(lastUpdatedAt) : null;
    }

    private BooleanExpression startDateGte(QEvalResultEntity eval, LocalDate startDate) {
        return startDate != null ? eval.createdAt.goe(startDate.atStartOfDay()) : null;
    }

    private BooleanExpression endDateLt(QEvalResultEntity eval, LocalDate endDate) {
        return endDate != null ? eval.createdAt.lt(endDate.plusDays(1).atStartOfDay()) : null;
    }
}