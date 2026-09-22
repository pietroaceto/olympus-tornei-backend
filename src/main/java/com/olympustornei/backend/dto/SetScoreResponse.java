package com.olympustornei.backend.dto;

public record SetScoreResponse(
        Integer setNumber,
        Integer homeGames,
        Integer awayGames
) {
}
