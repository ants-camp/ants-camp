package io.antcamp.assistantservice.presentation.controller;

import common.dto.ApiResponse;
import io.antcamp.assistantservice.application.dto.command.IngestDocumentCommand;
import io.antcamp.assistantservice.application.dto.command.UpdateDocumentCommand;
import io.antcamp.assistantservice.application.service.DocumentApplicationService;
import io.antcamp.assistantservice.domain.model.DocType;
import io.antcamp.assistantservice.presentation.dto.request.SaveDocumentRequest;
import io.antcamp.assistantservice.presentation.dto.response.DocumentDetailResponse;
import io.antcamp.assistantservice.presentation.dto.response.DocumentListResponse;
import io.antcamp.assistantservice.presentation.dto.response.DocumentUploadResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/assistants/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentApplicationService documentApplicationService;

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> ingestDocument(
            @Valid @RequestBody SaveDocumentRequest request
    ) {
        DocumentUploadResponse response = DocumentUploadResponse.from(
                documentApplicationService.ingestDocument(
                        new IngestDocumentCommand(request.title(), request.type(), request.content())
                )
        );
        return ApiResponse.created("문서 등록이 요청되었습니다. 처리 중입니다.", response);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> getDocument(@PathVariable UUID documentId) {
        return ApiResponse.ok(DocumentDetailResponse.from(documentApplicationService.getDocument(documentId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DocumentListResponse>> listDocuments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) DocType type,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastUpdatedAt
    ) {
        return ApiResponse.ok(DocumentListResponse.from(
                documentApplicationService.getDocuments(type, title, keyword, lastUpdatedAt)
        ));
    }

    @PutMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> updateDocument(
            @PathVariable UUID documentId,
            @Valid @RequestBody SaveDocumentRequest request
    ) {
        DocumentUploadResponse response = DocumentUploadResponse.from(
                documentApplicationService.updateDocument(
                        new UpdateDocumentCommand(documentId, request.title(), request.type(), request.content())
                )
        );
        return ApiResponse.ok(response);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID documentId) {
        documentApplicationService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }

}