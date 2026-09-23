package com.olympustornei.backend.dto;

import java.util.List;

public record MatchSubMatchScoreResponse(
        Integer ordine,
        List<SetScoreResponse> sets
) {
}
