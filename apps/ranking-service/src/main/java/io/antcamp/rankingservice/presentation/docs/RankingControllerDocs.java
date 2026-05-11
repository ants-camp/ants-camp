package io.antcamp.rankingservice.presentation.docs;

import io.antcamp.rankingservice.presentation.dto.FinalizeRankingsResponse;
import io.antcamp.rankingservice.presentation.dto.MyRankingResponse;
import io.antcamp.rankingservice.presentation.dto.RankingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Ranking", description = "대회 랭킹 조회 / 최종 순위 확정")
public interface RankingControllerDocs {

    @Operation(summary = "내 순위 조회", description = "대회 내 특정 사용자의 현재 순위와 수익률을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "competitionId": "550e8400-e29b-41d4-a716-446655440000",
                                      "userId": "user-uuid-...",
                                      "rank": 3,
                                      "returnRate": 12.54,
                                      "totalAsset": 11254000,
                                      "nickname": "투자왕",
                                      "isFinalized": false
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "랭킹 정보 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 404,
                                      "code": "RANKING_NOT_FOUND",
                                      "message": "랭킹 정보를 찾을 수 없습니다.",
                                      "data": null
                                    }""")))
    })
    @GetMapping("/competitions/{competitionId}/users/{userId}")
    MyRankingResponse findMyRanking(
            @Parameter(description = "대회 UUID", required = true) @PathVariable UUID competitionId,
            @Parameter(description = "사용자 UUID", required = true) @PathVariable UUID userId);

    @Operation(summary = "최종 순위 확정 (수동 트리거)", description = "대회 종료 후 최종 순위를 확정합니다. 관리자 전용.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확정 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    {
                                      "competitionId": "550e8400-e29b-41d4-a716-446655440000",
                                      "finalizedCount": 42
                                    }"""))),
            @ApiResponse(responseCode = "409", description = "이미 확정된 랭킹",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "실패", value = """
                                    {
                                      "status": 409,
                                      "code": "RANKING_ALREADY_FINALIZED",
                                      "message": "이미 확정된 랭킹입니다.",
                                      "data": null
                                    }""")))
    })
    @PostMapping("/competitions/{competitionId}/finalize")
    FinalizeRankingsResponse finalizeRankings(
            @Parameter(description = "대회 UUID", required = true) @PathVariable UUID competitionId);

    @Operation(summary = "대회 전체 랭킹 조회 (페이지)", description = "수익률 기준 내림차순으로 랭킹을 페이지 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = """
                                    [
                                      {
                                        "rank": 1,
                                        "userId": "user-uuid-001",
                                        "nickname": "주식고수",
                                        "returnRate": 25.30,
                                        "totalAsset": 12530000,
                                        "isFinalized": false
                                      },
                                      {
                                        "rank": 2,
                                        "userId": "user-uuid-002",
                                        "nickname": "투자왕",
                                        "returnRate": 12.54,
                                        "totalAsset": 11254000,
                                        "isFinalized": false
                                      }
                                    ]"""))),
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
    @GetMapping("/competitions/{competitionId}")
    List<RankingResponse> findTopRankings(
            @Parameter(description = "대회 UUID", required = true) @PathVariable UUID competitionId,
            @Parameter(description = "페이지 번호 (0부터 시작, 기본 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (기본 20)") @RequestParam(defaultValue = "20") int size);
}
