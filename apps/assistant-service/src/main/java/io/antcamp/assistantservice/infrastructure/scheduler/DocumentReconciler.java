package io.antcamp.assistantservice.infrastructure.scheduler;

import io.antcamp.assistantservice.application.port.IngestPort;
import io.antcamp.assistantservice.application.port.VectorStorePort;
import io.antcamp.assistantservice.domain.model.KnowledgeDocument;
import io.antcamp.assistantservice.domain.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentReconciler {

    private static final int PROCESSING_TIMEOUT_MINUTES = 10;
    private static final int MAX_RETRY = 3;

    private final KnowledgeDocumentRepository documentRepository;
    private final IngestPort ingestPort;
    private final VectorStorePort vectorStorePort;
    private final CleanupExecutor cleanupExecutor;

    @Scheduled(fixedDelay = 60_000)
    public void reconcile() {
        reconcileIngest();
        reconcileCleanup();
    }

    // PROCESSING 타임아웃 + FAILED 자동 재시도
    private void reconcileIngest() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(PROCESSING_TIMEOUT_MINUTES);
        List<KnowledgeDocument> targets = documentRepository.findReconcileTargets(threshold, MAX_RETRY);
        if (targets.isEmpty()) return;

        log.warn("인제스트 reconcile 대상 발견: {}건", targets.size());
        for (KnowledgeDocument doc : targets) {
            doc.markProcessing();
            documentRepository.save(doc);
            ingestPort.ingest(doc);
            log.warn("재인제스트 시작: documentId={}, retryCount={}", doc.getDocumentId(), doc.getRetryCount());
        }
    }

    // CLEANUP_PENDING 문서의 벡터 + 청크 + 문서 레코드 정리
    private void reconcileCleanup() {
        List<UUID> targets = documentRepository.findCleanupPendingIds();
        if (targets.isEmpty()) return;

        log.info("CLEANUP_PENDING 문서 정리 시작: {}건", targets.size());
        for (UUID documentId : targets) {
            try {
                vectorStorePort.deleteByDocumentId(documentId);
                cleanupExecutor.deleteDbRecords(documentId);
                log.info("문서 정리 완료: documentId={}", documentId);
            } catch (Exception e) {
                log.error("문서 정리 실패, 다음 주기 재시도: documentId={}", documentId, e);
            }
        }
    }
}