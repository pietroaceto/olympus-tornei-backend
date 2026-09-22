package com.olympustornei.backend.dto;

public record CategoryResponse(
        Long id,
        Long tournamentId,
        String name,
        String matchFormat,
        Integer subMatchesCount,
        String phase,
        boolean scheduleLocked
) {
}
