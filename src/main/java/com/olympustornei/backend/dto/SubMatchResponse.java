package com.olympustornei.backend.dto;

import java.util.List;

public record SubMatchResponse(
        Long id,
        Integer ordine,
        PlayerResponse homePlayer1,
        PlayerResponse homePlayer2,
        PlayerResponse awayPlayer1,
        PlayerResponse awayPlayer2,
        List<SetScoreResponse> sets,
        String setsWonBy
) {
}
