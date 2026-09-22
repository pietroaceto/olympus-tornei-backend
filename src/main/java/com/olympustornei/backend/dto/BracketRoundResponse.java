package com.olympustornei.backend.dto;

import java.util.List;

public record BracketRoundResponse(
        int roundIndex,
        List<BracketMatchResponse> matches
) {
}
