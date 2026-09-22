package com.olympustornei.backend.controller;

import com.olympustornei.backend.dto.MatchDetailResponse;
import com.olympustornei.backend.dto.MatchResultRequest;
import com.olympustornei.backend.service.MatchResultService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/matches")
public class MatchAdminController {

    private final MatchResultService matchResultService;

    public MatchAdminController(MatchResultService matchResultService) {
        this.matchResultService = matchResultService;
    }

    @PostMapping("/{matchId}/result")
    public MatchDetailResponse submitResult(@PathVariable Long matchId, @Valid @RequestBody MatchResultRequest request) {
        return matchResultService.submitResult(matchId, request);
    }
}
