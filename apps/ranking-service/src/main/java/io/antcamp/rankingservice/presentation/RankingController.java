package io.antcamp.rankingservice.presentation;

import common.dto.CommonResponse;
import io.antcamp.rankingservice.application.RankingService;
import io.antcamp.rankingservice.presentation.docs.RankingControllerDocs;
import io.antcamp.rankingservice.presentation.dto.FinalizeRankingsResponse;
import io.antcamp.rankingservice.presentation.dto.MyCompetitionHistoryResponse;
import io.antcamp.rankingservice.presentation.dto.MyRankingResponse;
import io.antcamp.rankingservice.presentation.dto.RankingResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rankings")
public class RankingController implements RankingControllerDocs {

    private final RankingService rankingService;

    // 내 순위 조회
    @GetMapping("/competitions/{competitionId}/me")
    public ResponseEntity<CommonResponse<MyRankingResponse>> findMyRanking(
            @PathVariable UUID competitionId,
            @RequestHeader("X-User-Id") UUID userId) {
        return CommonResponse.ok(MyRankingResponse.from(rankingService.findMyRanking(competitionId, userId)));
    }

    // 대회 종료 시 최종 순위 확정 (수동 트리거)
    @PostMapping("/competitions/{competitionId}/finalize")
    public ResponseEntity<CommonResponse<FinalizeRankingsResponse>> finalizeRankings(@PathVariable UUID competitionId) {
        int finalizedCount = rankingService.finalizeRankings(competitionId);
        return CommonResponse.ok("최종 순위가 확정되었습니다.", new FinalizeRankingsResponse(competitionId, finalizedCount));
    }

    // 대회 전체 랭킹 조회
    @GetMapping("/competitions/{competitionId}")
    public ResponseEntity<CommonResponse<List<RankingResponse>>> findTopRankings(
            @PathVariable UUID competitionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return CommonResponse.ok(rankingService.findTopRankings(competitionId, page, size)
                .stream()
                .map(RankingResponse::from)
                .toList());
    }

    // 내 대회 참여 이력 조회 (확정된 대회만)
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<List<MyCompetitionHistoryResponse>>> findMyHistory(
            @RequestHeader("X-User-Id") UUID userId) {
        return CommonResponse.ok(rankingService.findMyHistory(userId)
                .stream()
                .map(MyCompetitionHistoryResponse::from)
                .toList());
    }
}
