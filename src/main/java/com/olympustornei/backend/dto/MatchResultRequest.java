package com.olympustornei.backend.dto;

import com.olympustornei.backend.domain.MatchResultType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record MatchResultRequest(
        @NotEmpty @Valid List<SubMatchRequest> subMatches,
        @NotNull MatchResultType resultType
) {
}
