package io.antcamp.competitionservice.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record JoinCompetitionRequest(
        @NotNull UUID userId,       // TODO: 인증 연동 후 헤더/토큰에서 추출로 교체
        @NotBlank String nickname   // TODO: 인증 연동 후 헤더/토큰에서 추출로 교체
) {
}
