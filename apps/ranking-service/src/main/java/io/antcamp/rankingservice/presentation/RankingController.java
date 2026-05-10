package io.antcamp.rankingservice.presentation;

import common.dto.ApiResponse;
import io.antcamp.rankingservice.application.RankingService;
import io.antcamp.rankingservice.presentation.dto.FinalizeRankingsResponse;
import io.antcamp.rankingservice.presentation.dto.MyRankingResponse;
import io.antcamp.rankingservice.presentation.dto.RankingResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rankings")
public class RankingController {

    private final RankingService rankingService;

    // 내 순위 조회
    @GetMapping("/competitions/{competitionId}/users/{userId}")
    public ResponseEntity<ApiResponse<MyRankingResponse>> findMyRanking(
            @PathVariable UUID competitionId,
            @PathVariable UUID userId) {
        return ApiResponse.ok(MyRankingResponse.from(rankingService.findMyRanking(competitionId, userId)));
    }

    // 대회 종료 시 최종 순위 확정 (수동 트리거)
    @PostMapping("/competitions/{competitionId}/finalize")
    public ResponseEntity<ApiResponse<FinalizeRankingsResponse>> finalizeRankings(@PathVariable UUID competitionId) {
        int finalizedCount = rankingService.finalizeRankings(competitionId);
        return ApiResponse.ok("최종 순위가 확정되었습니다.", new FinalizeRankingsResponse(competitionId, finalizedCount));
    }

    // 대회 전체 랭킹 조회
    @GetMapping("/competitions/{competitionId}")
    public ResponseEntity<ApiResponse<List<RankingResponse>>> findTopRankings(
            @PathVariable UUID competitionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(rankingService.findTopRankings(competitionId, page, size)
                .stream()
                .map(RankingResponse::from)
                .toList());
    }
}
