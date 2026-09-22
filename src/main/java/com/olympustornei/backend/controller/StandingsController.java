package com.olympustornei.backend.controller;

import com.olympustornei.backend.dto.StandingRowResponse;
import com.olympustornei.backend.service.StandingsService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/categories/{categoryId}/standings")
public class StandingsController {

    private final StandingsService standingsService;

    public StandingsController(StandingsService standingsService) {
        this.standingsService = standingsService;
    }

    @GetMapping
    public List<StandingRowResponse> getStandings(@PathVariable Long categoryId) {
        return standingsService.getStandings(categoryId);
    }
}
