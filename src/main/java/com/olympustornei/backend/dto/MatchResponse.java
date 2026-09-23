package com.olympustornei.backend.dto;

public record MatchResponse(
        Long id,
        Long homeTeamId,
        String homeTeamName,
        Long awayTeamId,
        String awayTeamName,
        String status,
        String resultType,
        Long winnerTeamId,
        String resultSummary
) {
}
