package io.antcamp.assistantservice.infrastructure.scheduler;

import io.antcamp.assistantservice.application.port.VectorStorePort;
import io.antcamp.assistantservice.domain.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupReconciler {

    private final KnowledgeDocumentRepository documentRepository;
    private final VectorStorePort vectorStorePort;
    private final CleanupExecutor cleanupExecutor;

    /**
     * CLEANUP_PENDING 문서의 벡터 + 청크 + 문서 레코드를 순차적으로 정리.
     * 벡터 삭제 후 청크·문서 삭제를 하나의 트랜잭션으로 처리.
     */
    @Scheduled(fixedDelay = 60_000)
    public void reconcile() {
        List<UUID> targets = documentRepository.findCleanupPendingIds();
        if (targets.isEmpty()) {
            return;
        }

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