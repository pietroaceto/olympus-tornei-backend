package com.olympustornei.backend.dto;

import java.util.List;

public record BracketResponse(
        Integer totalRounds,
        List<BracketRoundResponse> rounds
) {
}
