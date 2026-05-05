package io.antcamp.assistantservice.infrastructure.vector;

import io.antcamp.assistantservice.domain.model.DocumentChunk;
import io.antcamp.assistantservice.domain.model.KnowledgeDocument;
import io.antcamp.assistantservice.domain.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChunkPersistenceHelper {

    private final KnowledgeDocumentRepository documentRepository;

    @Transactional
    public List<DocumentChunk> splitAndSave(KnowledgeDocument document, TokenTextSplitter splitter) {
        // 재인제스트 멱등성: 이전 실패로 남은 청크를 제거하고 새로 저장
        documentRepository.deleteChunksByDocumentId(document.getDocumentId());

        Document doc = new Document(document.getContent(), Map.of("documentId", document.getDocumentId().toString()));
        List<Document> splitDocs = splitter.apply(List.of(doc));

        List<DocumentChunk> chunks = new ArrayList<>();
        for (int i = 0; i < splitDocs.size(); i++) {
            chunks.add(DocumentChunk.create(document.getDocumentId(), i, splitDocs.get(i).getText()));
        }
        return documentRepository.saveChunks(chunks);
    }

    @Transactional
    public void markCompleted(UUID documentId) {
        documentRepository.findById(documentId).ifPresent(doc -> {
            doc.markCompleted();
            documentRepository.save(doc);
        });
    }

    @Transactional
    public void markFailed(UUID documentId, String reason) {
        documentRepository.findById(documentId).ifPresent(doc -> {
            doc.markFailed(reason);
            documentRepository.save(doc);
        });
    }
}