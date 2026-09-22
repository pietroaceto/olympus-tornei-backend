package com.olympustornei.backend.dto;

public record StandingRowResponse(
        int position,
        Long teamId,
        String teamName,
        int played,
        int won,
        int lost,
        int points,
        int setsWon,
        int setsLost,
        int setDiff,
        int gamesWon,
        int gamesLost,
        int gameDiff
) {
}
