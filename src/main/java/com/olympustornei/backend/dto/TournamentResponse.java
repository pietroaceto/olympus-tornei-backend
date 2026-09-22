package com.olympustornei.backend.dto;

public record TournamentResponse(
        Long id,
        String name,
        String season,
        String status
) {
}
