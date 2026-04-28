package io.antcamp.assistantservice.infrastructure.vector;

import io.antcamp.assistantservice.application.port.IngestPort;
import io.antcamp.assistantservice.application.port.VectorStorePort;
import io.antcamp.assistantservice.domain.model.DocumentChunk;
import io.antcamp.assistantservice.domain.model.KnowledgeDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentIngestor implements IngestPort {

    private final VectorStorePort vectorStorePort;
    private final TokenTextSplitter tokenTextSplitter;
    private final ChunkPersistenceHelper chunkPersistenceHelper;

    @Async("ingestExecutor")
    @Override
    public void ingest(KnowledgeDocument document) {
        log.info("문서 인제스트 시작: documentId={}", document.getDocumentId());

        // 재인제스트 멱등성: 이전 실패의 잔여 벡터 정리 (실패해도 계속 진행)
        try {
            vectorStorePort.deleteByDocumentId(document.getDocumentId());
        } catch (Exception e) {
            log.warn("이전 벡터 정리 실패 (계속 진행): documentId={}", document.getDocumentId(), e);
        }

        List<DocumentChunk> savedChunks;
        try {
            savedChunks = chunkPersistenceHelper.splitAndSave(document, tokenTextSplitter);
        } catch (Exception e) {
            log.error("청킹 실패: documentId={}", document.getDocumentId(), e);
            chunkPersistenceHelper.markFailed(document.getDocumentId(), classify(e));
            return;
        }

        try {
            storeInVectorStore(savedChunks, document);
        } catch (Exception e) {
            log.error("벡터 저장 실패: documentId={}, 청크 수={}", document.getDocumentId(), savedChunks.size(), e);
            chunkPersistenceHelper.markFailed(document.getDocumentId(), classify(e));
            return;
        }

        chunkPersistenceHelper.markCompleted(document.getDocumentId());
        log.info("문서 인제스트 완료: documentId={}, 청크 수={}", document.getDocumentId(), savedChunks.size());
    }

    private void storeInVectorStore(List<DocumentChunk> chunks, KnowledgeDocument document) {
        List<VectorStorePort.ChunkToStore> toStore = chunks.stream()
                .map(c -> new VectorStorePort.ChunkToStore(
                        c.getDocumentChunkId(),
                        c.getKnowledgeDocumentId(),
                        document.getTitle(),
                        document.getType().name(),
                        c.getContent()
                ))
                .toList();
        vectorStorePort.store(toStore);
    }

    private String classify(Exception e) {
        return IngestRetryPolicy.classify(e);
    }
}