package com.olympustornei.backend.dto;

public record BracketMatchResponse(
        Long matchId,
        Integer slot,
        Long homeTeamId,
        String homeTeamName,
        Long awayTeamId,
        String awayTeamName,
        String status,
        String resultType,
        Long winnerTeamId
) {
}
