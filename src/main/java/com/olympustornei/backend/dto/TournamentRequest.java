package com.olympustornei.backend.dto;

import com.olympustornei.backend.domain.TournamentStatus;
import jakarta.validation.constraints.NotBlank;

public record TournamentRequest(
        @NotBlank String name,
        String season,
        TournamentStatus status
) {
}
