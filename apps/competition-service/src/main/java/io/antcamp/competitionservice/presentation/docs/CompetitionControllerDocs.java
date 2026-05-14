package io.antcamp.competitionservice.presentation.docs;

import common.dto.CommonResponse;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import io.antcamp.competitionservice.presentation.dto.request.CreateCompetitionRequest;
import io.antcamp.competitionservice.presentation.dto.request.UpdateCompetitionRequest;
import io.antcamp.competitionservice.presentation.dto.response.CreateCompetitionResponse;
import io.antcamp.competitionservice.presentation.dto.response.FindCompetitionChangeNoticeResponse;
import io.antcamp.competitionservice.presentation.dto.response.FindCompetitionParticipantResponse;
import io.antcamp.competitionservice.presentation.dto.response.FindCompetitionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Competition", description = "대회 생성·조회·상태전환 / 참가 신청·취소")
public interface CompetitionControllerDocs {

    // ─── 대회 ─────────────────────────────────────────────────────────────────

    @Operation(summary = "대회 생성 (관리자)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "대회 생성 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "competitionId": "550e8400-e29b-41d4-a716-446655440000",
                                      "name": "2026 봄 모의투자 대회",
                                      "type": "MOCK_INVESTMENT",
                                      "status": "DRAFT",
                                      "firstSeed": 10000000,
                                      "minParticipants": 5,
                                      "maxParticipants": 100,
                                      "registerStartAt": "2026-05-01T00:00:00",
                                      "registerEndAt": "2026-05-20T23:59:59",
                                      "competitionStartAt": "2026-06-01T09:00:00",
                                      "competitionEndAt": "2026-06-30T15:30:00"
                                    }"""))),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 400,
                                      "code": "INVALID_INPUT",
                                      "message": "입력값이 유효하지 않습니다.",
                                      "data": null
                                    }""")))
    })
    @PostMapping
    ResponseEntity<CommonResponse<CreateCompetitionResponse>> createCompetition(@RequestBody @Valid CreateCompetitionRequest request);

    @Operation(summary = "대회 단건 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "competitionId": "550e8400-e29b-41d4-a716-446655440000",
                                      "name": "2026 봄 모의투자 대회",
                                      "status": "OPEN",
                                      "firstSeed": 10000000
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 대회",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 404,
                                      "code": "COMPETITION_NOT_FOUND",
                                      "message": "존재하지 않는 대회입니다.",
                                      "data": null
                                    }""")))
    })
    @GetMapping("/{id}")
    ResponseEntity<CommonResponse<FindCompetitionResponse>> findCompetitionById(
            @Parameter(description = "대회 UUID", required = true) @PathVariable UUID id);

    @Operation(summary = "대회 공개 (DRAFT → OPEN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공개 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {"competitionId":"550e8400-...","status":"OPEN"}"""))),
            @ApiResponse(responseCode = "409", description = "이미 공개된 대회",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":409,"code":"COMPETITION_ALREADY_PUBLISHED","message":"이미 공개된 대회입니다.","data":null}""")))
    })
    @PostMapping("/{id}/publications")
    ResponseEntity<CommonResponse<FindCompetitionResponse>> openCompetition(
            @Parameter(description = "대회 UUID", required = true) @PathVariable UUID id);


    @Operation(summary = "대회 시작 (OPEN → IN_PROGRESS)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "시작 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {"competitionId":"550e8400-...","status":"IN_PROGRESS"}"""))),
            @ApiResponse(responseCode = "400", description = "최소 참가자 수 미충족",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":400,"code":"COMPETITION_MIN_PARTICIPANTS_NOT_MET","message":"최소 참가자 수를 충족하지 못해 대회를 시작할 수 없습니다.","data":null}""")))
    })
    @PostMapping("/{id}/starts")
    ResponseEntity<CommonResponse<FindCompetitionResponse>> startCompetition(
            @Parameter(description = "대회 UUID", required = true) @PathVariable UUID id);

    @Operation(summary = "대회 종료 (IN_PROGRESS → FINISHED)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "종료 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {"competitionId":"550e8400-...","status":"FINISHED"}"""))),
            @ApiResponse(responseCode = "409", description = "이미 종료된 대회",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":409,"code":"COMPETITION_ALREADY_FINISHED","message":"이미 종료된 대회입니다.","data":null}""")))
    })
    @PostMapping("/{id}/finishes")
    ResponseEntity<CommonResponse<FindCompetitionResponse>> finishCompetition(
            @Parameter(description = "대회 UUID", required = true) @PathVariable UUID id);

    @Operation(summary = "대회 취소 (→ CANCELLED)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "취소 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {"competitionId":"550e8400-...","status":"CANCELLED"}"""))),
            @ApiResponse(responseCode = "409", description = "허용되지 않는 상태 전환",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":409,"code":"COMPETITION_INVALID_STATUS","message":"현재 대회 상태에서 허용되지 않는 작업입니다.","data":null}""")))
    })
    @PostMapping("/{id}/cancellations")
    ResponseEntity<CommonResponse<FindCompetitionResponse>> cancelCompetition(
            @Parameter(description = "대회 UUID", required = true) @PathVariable UUID id);

    @Operation(summary = "대회 삭제 (소프트 삭제)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {"competitionId":"550e8400-...","status":"DELETED"}"""))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 대회",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":404,"code":"COMPETITION_NOT_FOUND","message":"존재하지 않는 대회입니다.","data":null}""")))
    })
    @DeleteMapping("/{id}")
    ResponseEntity<CommonResponse<FindCompetitionResponse>> deleteCompetition(
            @Parameter(description = "대회 UUID", required = true) @PathVariable UUID id,
            @Parameter(description = "삭제 요청자 식별자", required = true) @RequestHeader UUID deletedBy);

    @Operation(summary = "대회 목록 조회 (페이지)", description = "status 파라미터로 필터링 가능. 생략 시 전체 반환.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "content": [{"competitionId":"550e8400-...","name":"2026 봄 모의투자 대회","status":"OPEN"}],
                                      "totalElements": 1,
                                      "totalPages": 1,
                                      "size": 10,
                                      "number": 0
                                    }"""))),
            @ApiResponse(responseCode = "400", description = "잘못된 status 값",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":400,"code":"INVALID_INPUT","message":"입력값이 유효하지 않습니다.","data":null}""")))
    })
    @GetMapping
    ResponseEntity<CommonResponse<Page<FindCompetitionResponse>>> findAllCompetition(
            @Parameter(description = "대회 상태 필터 (DRAFT/OPEN/IN_PROGRESS/FINISHED/CANCELLED)")
            @RequestParam(required = false) CompetitionStatus status,
            Pageable pageable);

    // ─── 변경 공지 ────────────────────────────────────────────────────────────

    @Operation(summary = "대회 변경 공지 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    [
                                      {
                                        "noticeId": "notice-uuid-...",
                                        "reason": "일정 변경",
                                        "updatedBy": "admin",
                                        "changedAt": "2026-05-10T12:00:00"
                                      }
                                    ]"""))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 대회",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":404,"code":"COMPETITION_NOT_FOUND","message":"존재하지 않는 대회입니다.","data":null}""")))
    })
    @GetMapping("/{id}/change-notices")
    ResponseEntity<CommonResponse<List<FindCompetitionChangeNoticeResponse>>> findAllCompetitionChangeNotice(
            @Parameter(description = "대회 UUID", required = true) @PathVariable UUID id);

    // ─── 참가자 ───────────────────────────────────────────────────────────────

    @Operation(summary = "대회 참가자 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    [
                                      {
                                        "participantId": "participant-uuid-...",
                                        "userId": "user-uuid-...",
                                        "nickname": "투자왕",
                                        "registeredAt": "2026-05-11T14:00:00"
                                      }
                                    ]"""))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 대회",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {"status":404,"code":"COMPETITION_NOT_FOUND","message":"존재하지 않는 대회입니다.","data":null}""")))
    })
    @GetMapping("/{competitionId}/participants")
    ResponseEntity<CommonResponse<List<FindCompetitionParticipantResponse>>> findCompetitionParticipants(
            @Parameter(description = "대회 UUID", required = true) @PathVariable UUID competitionId);
}
