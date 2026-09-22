package com.olympustornei.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SubMatchRequest(
        @NotNull Long homePlayer1Id,
        @NotNull Long homePlayer2Id,
        @NotNull Long awayPlayer1Id,
        @NotNull Long awayPlayer2Id,
        @NotEmpty @Valid List<SetScoreRequest> sets
) {
}
