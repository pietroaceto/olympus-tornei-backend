package com.olympustornei.backend.dto;

import java.util.List;

public record MatchDetailResponse(
        Long id,
        Long categoryId,
        String phase,
        Long homeTeamId,
        String homeTeamName,
        Long awayTeamId,
        String awayTeamName,
        String status,
        String resultType,
        Long winnerTeamId,
        String suggestedWinner,
        List<SubMatchResponse> subMatches
) {
}
