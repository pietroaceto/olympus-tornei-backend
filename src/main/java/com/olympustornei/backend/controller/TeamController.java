package com.olympustornei.backend.controller;

import com.olympustornei.backend.dto.TeamResponse;
import com.olympustornei.backend.service.TeamService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/categories/{categoryId}/teams")
    public List<TeamResponse> listByCategory(@PathVariable Long categoryId) {
        return teamService.listByCategory(categoryId);
    }

    @GetMapping("/teams/{id}")
    public TeamResponse getById(@PathVariable Long id) {
        return teamService.getById(id);
    }
}
