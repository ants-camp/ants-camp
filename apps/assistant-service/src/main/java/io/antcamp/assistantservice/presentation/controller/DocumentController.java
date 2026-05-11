package io.antcamp.assistantservice.presentation.controller;

import common.dto.CommonResponse;
import io.antcamp.assistantservice.application.dto.command.IngestDocumentCommand;
import io.antcamp.assistantservice.application.dto.command.UpdateDocumentCommand;
import io.antcamp.assistantservice.application.service.DocumentApplicationService;
import io.antcamp.assistantservice.domain.model.DocType;
import io.antcamp.assistantservice.infrastructure.security.ManagerRoleGuard;
import io.antcamp.assistantservice.presentation.dto.request.SaveDocumentRequest;
import io.antcamp.assistantservice.presentation.dto.response.DocumentDetailResponse;
import io.antcamp.assistantservice.presentation.dto.response.DocumentListResponse;
import io.antcamp.assistantservice.presentation.dto.response.DocumentUploadResponse;
import jakarta.validation.Valid;
import io.antcamp.assistantservice.presentation.controller.docs.DocumentControllerDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/assistants/documents")
@RequiredArgsConstructor
public class DocumentController implements DocumentControllerDocs {

    private final DocumentApplicationService documentApplicationService;
    private final ManagerRoleGuard managerRoleGuard;

    @PostMapping
    public ResponseEntity<CommonResponse<DocumentUploadResponse>> ingestDocument(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody SaveDocumentRequest request
    ) {
        managerRoleGuard.require(role);
        DocumentUploadResponse response = DocumentUploadResponse.from(
                documentApplicationService.ingestDocument(
                        new IngestDocumentCommand(request.title(), request.type(), request.content())
                )
        );
        return CommonResponse.created("문서 등록이 요청되었습니다. 처리 중입니다.", response);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<CommonResponse<DocumentDetailResponse>> getDocument(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID documentId
    ) {
        managerRoleGuard.require(role);
        return CommonResponse.ok(DocumentDetailResponse.from(documentApplicationService.getDocument(documentId)));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<DocumentListResponse>> listDocuments(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) DocType type,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastUpdatedAt
    ) {
        managerRoleGuard.require(role);
        return CommonResponse.ok(DocumentListResponse.from(
                documentApplicationService.getDocuments(type, title, keyword, lastUpdatedAt)
        ));
    }

    @PutMapping("/{documentId}")
    public ResponseEntity<CommonResponse<DocumentUploadResponse>> updateDocument(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID documentId,
            @Valid @RequestBody SaveDocumentRequest request
    ) {
        managerRoleGuard.require(role);
        DocumentUploadResponse response = DocumentUploadResponse.from(
                documentApplicationService.updateDocument(
                        new UpdateDocumentCommand(documentId, request.title(), request.type(), request.content())
                )
        );
        return CommonResponse.ok(response);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID documentId
    ) {
        managerRoleGuard.require(role);
        documentApplicationService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }

}