package io.antcamp.assistantservice.infrastructure.scheduler;

import io.antcamp.assistantservice.domain.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CleanupExecutor {

    private final KnowledgeDocumentRepository documentRepository;

    @Transactional
    public void deleteDbRecords(UUID documentId) {
        documentRepository.deleteChunksByDocumentId(documentId);
        documentRepository.deleteById(documentId);
    }
}