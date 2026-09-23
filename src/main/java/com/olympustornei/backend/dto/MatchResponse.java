package com.olympustornei.backend.dto;

import java.util.List;

public record MatchResponse(
        Long id,
        Long homeTeamId,
        String homeTeamName,
        Long awayTeamId,
        String awayTeamName,
        String status,
        String resultType,
        Long winnerTeamId,
        List<MatchSubMatchScoreResponse> subMatches
) {
}
