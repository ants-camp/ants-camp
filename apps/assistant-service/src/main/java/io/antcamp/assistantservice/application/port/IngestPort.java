package io.antcamp.assistantservice.application.port;

import io.antcamp.assistantservice.domain.model.KnowledgeDocument;

public interface IngestPort {

    void ingest(KnowledgeDocument document);
}