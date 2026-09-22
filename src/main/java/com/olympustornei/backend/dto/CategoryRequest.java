package com.olympustornei.backend.dto;

import com.olympustornei.backend.domain.CategoryName;
import com.olympustornei.backend.domain.MatchFormat;
import jakarta.validation.constraints.NotNull;

public record CategoryRequest(
        @NotNull CategoryName name,
        @NotNull MatchFormat matchFormat,
        Integer subMatchesCount
) {
}
