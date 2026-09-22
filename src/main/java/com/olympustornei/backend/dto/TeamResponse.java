package com.olympustornei.backend.dto;

import java.util.List;

public record TeamResponse(
        Long id,
        Long categoryId,
        String name,
        List<PlayerResponse> players
) {
}
