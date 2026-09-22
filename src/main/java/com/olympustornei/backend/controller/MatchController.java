package com.olympustornei.backend.controller;

import com.olympustornei.backend.dto.MatchDetailResponse;
import com.olympustornei.backend.service.MatchResultService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/matches")
public class MatchController {

    private final MatchResultService matchResultService;

    public MatchController(MatchResultService matchResultService) {
        this.matchResultService = matchResultService;
    }

    @GetMapping("/{id}")
    public MatchDetailResponse getById(@PathVariable Long id) {
        return matchResultService.getById(id);
    }
}
