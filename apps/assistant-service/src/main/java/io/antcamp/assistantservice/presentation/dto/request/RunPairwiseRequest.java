package io.antcamp.assistantservice.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record RunPairwiseRequest(
        @NotNull(message = "비교할 Run A의 ID는 필수입니다.")
        UUID evalRunIdA,

        @NotNull(message = "비교할 Run B의 ID는 필수입니다.")
        UUID evalRunIdB,

        @NotEmpty(message = "평가 모델은 최소 1개 이상이어야 합니다.")
        List<String> judgeModels
) {}