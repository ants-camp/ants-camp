package io.antcamp.assistantservice.application.service;

import io.antcamp.assistantservice.domain.model.CursorSlice;
import io.antcamp.assistantservice.application.dto.command.IngestDocumentCommand;
import io.antcamp.assistantservice.application.dto.command.UpdateDocumentCommand;
import io.antcamp.assistantservice.application.dto.result.DocumentDetailResult;
import io.antcamp.assistantservice.application.dto.result.DocumentItemResult;
import io.antcamp.assistantservice.application.dto.result.DocumentListResult;
import io.antcamp.assistantservice.application.dto.result.DocumentUploadResult;
import io.antcamp.assistantservice.application.port.IngestPort;
import io.antcamp.assistantservice.domain.exception.DocumentNotFoundException;
import io.antcamp.assistantservice.domain.model.DocType;
import io.antcamp.assistantservice.domain.model.KnowledgeDocument;
import io.antcamp.assistantservice.domain.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentApplicationService {

    private static final int PAGE_SIZE = 30;

    private final KnowledgeDocumentRepository documentRepository;
    private final IngestPort ingestPort;

    @Transactional
    public DocumentUploadResult ingestDocument(IngestDocumentCommand command) {
        KnowledgeDocument document = KnowledgeDocument.create(command.title(), command.type(), command.content());
        KnowledgeDocument saved = documentRepository.save(document);

        // 트랜잭션 커밋 후 비동기 실행. DB에 문서가 저장된 뒤 인제스트 시작
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                ingestPort.ingest(saved);
            }
        });

        log.info("문서 등록 요청: documentId={}, title={}", saved.getDocumentId(), saved.getTitle());
        return DocumentUploadResult.from(saved);
    }

    @Transactional
    public DocumentUploadResult updateDocument(UpdateDocumentCommand command) {
        KnowledgeDocument document = documentRepository.findAccessibleById(command.documentId())
                .orElseThrow(DocumentNotFoundException::new);

        // 기존 청크 삭제
        documentRepository.deleteChunksByDocumentId(command.documentId());

        document.update(command.title(), command.type(), command.content());
        KnowledgeDocument updated = documentRepository.save(document);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                ingestPort.ingest(updated);
            }
        });

        log.info("문서 수정 요청: documentId={}", updated.getDocumentId());
        return DocumentUploadResult.from(updated);
    }

    @Transactional
    public void deleteDocument(UUID documentId) {
        KnowledgeDocument document = documentRepository.findById(documentId)
                .orElseThrow(DocumentNotFoundException::new);
        document.markCleanupPending();
        documentRepository.save(document);
        log.info("문서 삭제 요청 (CLEANUP_PENDING 전환): documentId={}", documentId);
    }

    @Transactional(readOnly = true)
    public DocumentDetailResult getDocument(UUID documentId) {
        KnowledgeDocument document = documentRepository.findAccessibleById(documentId)
                .orElseThrow(DocumentNotFoundException::new);
        int chunkCount = documentRepository.countChunksByDocumentId(documentId);
        return DocumentDetailResult.from(document, chunkCount);
    }

    @Transactional(readOnly = true)
    public DocumentListResult getDocuments(DocType type, String title, String keyword, LocalDateTime lastUpdatedAt) {
        CursorSlice<KnowledgeDocument, LocalDateTime> slice =
                documentRepository.findDocuments(type, title, keyword, lastUpdatedAt, PAGE_SIZE);

        return new DocumentListResult(
                slice.items().stream().map(DocumentItemResult::from).toList(),
                slice.hasNext(),
                slice.nextCursor()
        );
    }
}