package io.antcamp.assistantservice.infrastructure.scheduler;

import io.antcamp.assistantservice.application.port.IngestPort;
import io.antcamp.assistantservice.domain.model.KnowledgeDocument;
import io.antcamp.assistantservice.domain.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestReconciler {

    private static final int PROCESSING_TIMEOUT_MINUTES = 10;
    private static final int MAX_RETRY = 3;

    private final KnowledgeDocumentRepository documentRepository;
    private final IngestPort ingestPort;

    /**
     * PROCESSING 타임아웃 + FAILED 자동 재시도.
     */
    @Scheduled(fixedDelay = 60_000)
    public void reconcile() {
        LocalDateTime processingThreshold = LocalDateTime.now().minusMinutes(PROCESSING_TIMEOUT_MINUTES);
        List<KnowledgeDocument> targets = documentRepository.findReconcileTargets(processingThreshold, MAX_RETRY);

        if (targets.isEmpty()) {
            return;
        }

        log.warn("인제스트 reconcile 대상 발견: {}건", targets.size());
        for (KnowledgeDocument doc : targets) {
            doc.markProcessing();
            documentRepository.save(doc);
            ingestPort.ingest(doc);
            log.warn("재인제스트 시작: documentId={}, retryCount={}", doc.getDocumentId(), doc.getRetryCount());
        }
    }
}