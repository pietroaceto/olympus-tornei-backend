package com.olympustornei.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SetScoreRequest(
        @NotNull @Min(1) Integer setNumber,
        @NotNull @Min(0) Integer homeGames,
        @NotNull @Min(0) Integer awayGames
) {
}
