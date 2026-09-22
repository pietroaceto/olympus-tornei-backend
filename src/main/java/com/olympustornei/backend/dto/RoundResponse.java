package com.olympustornei.backend.dto;

import java.util.List;

public record RoundResponse(
        Integer roundNumber,
        List<MatchResponse> matches
) {
}
