package io.antcamp.assistantservice.presentation.controller.docs;

import common.dto.CommonResponse;
import io.antcamp.assistantservice.domain.model.DocType;
import io.antcamp.assistantservice.presentation.dto.request.SaveDocumentRequest;
import io.antcamp.assistantservice.presentation.dto.response.DocumentDetailResponse;
import io.antcamp.assistantservice.presentation.dto.response.DocumentListResponse;
import io.antcamp.assistantservice.presentation.dto.response.DocumentUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Tag(name = "Assistant - Document", description = "RAG 문서 등록·조회·수정·삭제 (MANAGER 전용)")
public interface DocumentControllerDocs {

    @Operation(summary = "문서 등록 (RAG 인덱싱)", description = "문서를 등록하고 벡터 DB에 인덱싱을 요청합니다. 처리는 비동기입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "문서 등록 요청 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 201,
                                      "code": "SUCCESS",
                                      "message": "문서 등록이 요청되었습니다. 처리 중입니다.",
                                      "data": {
                                        "documentId": "doc-uuid-...",
                                        "title": "삼성전자 2026 사업전망",
                                        "type": "STOCK_ANALYSIS",
                                        "status": "PROCESSING"
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "400", description = "제목 또는 내용 유효성 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 400,
                                      "code": "DOCUMENT_TITLE_BLANK",
                                      "message": "문서 제목은 비어있을 수 없습니다.",
                                      "data": null
                                    }""")))
    })
    @PostMapping
    ResponseEntity<CommonResponse<DocumentUploadResponse>> ingestDocument(
            @Parameter(description = "X-User-Role", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Role") String role,
            @RequestBody SaveDocumentRequest request);

    @Operation(summary = "문서 단건 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "요청에 성공했습니다.",
                                      "data": {
                                        "documentId": "doc-uuid-...",
                                        "title": "삼성전자 2026 사업전망",
                                        "type": "STOCK_ANALYSIS",
                                        "content": "삼성전자는 2026년 반도체 부문에서...",
                                        "status": "INDEXED",
                                        "createdAt": "2026-05-10T10:00:00"
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 문서",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 404,
                                      "code": "DOCUMENT_NOT_FOUND",
                                      "message": "존재하지 않는 문서입니다.",
                                      "data": null
                                    }""")))
    })
    @GetMapping("/{documentId}")
    ResponseEntity<CommonResponse<DocumentDetailResponse>> getDocument(
            @Parameter(description = "X-User-Role", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Role") String role,
            @Parameter(description = "문서 UUID", required = true) @PathVariable UUID documentId);

    @Operation(summary = "문서 목록 조회", description = "키워드·타입·제목·커서 기반 필터링을 지원합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "요청에 성공했습니다.",
                                      "data": {
                                        "documents": [
                                          {
                                            "documentId": "doc-uuid-...",
                                            "title": "삼성전자 2026 사업전망",
                                            "type": "STOCK_ANALYSIS",
                                            "status": "INDEXED",
                                            "updatedAt": "2026-05-10T10:00:00"
                                          }
                                        ],
                                        "hasNext": false
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "MANAGER 권한 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":403,"code":"FORBIDDEN","message":"접근 권한이 없습니다.","data":null}""")))
    })
    @GetMapping
    ResponseEntity<CommonResponse<DocumentListResponse>> listDocuments(
            @Parameter(description = "X-User-Role", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Role") String role,
            @Parameter(description = "키워드 검색") @RequestParam(required = false) String keyword,
            @Parameter(description = "문서 타입 필터") @RequestParam(required = false) DocType type,
            @Parameter(description = "제목 검색") @RequestParam(required = false) String title,
            @Parameter(description = "커서 (마지막 updatedAt, ISO_DATE_TIME)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastUpdatedAt);

    @Operation(summary = "문서 수정 및 재인덱싱", description = "문서 내용을 수정하고 벡터 DB를 갱신합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS",
                                      "message": "요청에 성공했습니다.",
                                      "data": {
                                        "documentId": "doc-uuid-...",
                                        "title": "삼성전자 2026 사업전망 (수정)",
                                        "status": "PROCESSING"
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 문서",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":404,"code":"DOCUMENT_NOT_FOUND","message":"존재하지 않는 문서입니다.","data":null}""")))
    })
    @PutMapping("/{documentId}")
    ResponseEntity<CommonResponse<DocumentUploadResponse>> updateDocument(
            @Parameter(description = "X-User-Role", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Role") String role,
            @Parameter(description = "문서 UUID", required = true) @PathVariable UUID documentId,
            @RequestBody SaveDocumentRequest request);

    @Operation(summary = "문서 삭제", description = "문서와 벡터 DB 인덱스를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 문서",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":404,"code":"DOCUMENT_NOT_FOUND","message":"존재하지 않는 문서입니다.","data":null}""")))
    })
    @DeleteMapping("/{documentId}")
    ResponseEntity<Void> deleteDocument(
            @Parameter(description = "X-User-Role", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-User-Role") String role,
            @Parameter(description = "문서 UUID", required = true) @PathVariable UUID documentId);
}
