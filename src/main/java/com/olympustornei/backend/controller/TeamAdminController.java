package com.olympustornei.backend.controller;

import com.olympustornei.backend.dto.PlayerRequest;
import com.olympustornei.backend.dto.PlayerResponse;
import com.olympustornei.backend.dto.TeamCreateRequest;
import com.olympustornei.backend.dto.TeamResponse;
import com.olympustornei.backend.dto.TeamUpdateRequest;
import com.olympustornei.backend.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class TeamAdminController {

    private final TeamService teamService;

    public TeamAdminController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping("/categories/{categoryId}/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse create(@PathVariable Long categoryId, @Valid @RequestBody TeamCreateRequest request) {
        return teamService.create(categoryId, request);
    }

    @PutMapping("/teams/{id}")
    public TeamResponse update(@PathVariable Long id, @Valid @RequestBody TeamUpdateRequest request) {
        return teamService.update(id, request);
    }

    @DeleteMapping("/teams/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        teamService.delete(id);
    }

    @PostMapping("/teams/{teamId}/players")
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse addPlayer(@PathVariable Long teamId, @Valid @RequestBody PlayerRequest request) {
        return teamService.addPlayer(teamId, request);
    }

    @PutMapping("/players/{id}")
    public PlayerResponse updatePlayer(@PathVariable Long id, @Valid @RequestBody PlayerRequest request) {
        return teamService.updatePlayer(id, request);
    }

    @DeleteMapping("/players/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlayer(@PathVariable Long id) {
        teamService.deletePlayer(id);
    }
}
