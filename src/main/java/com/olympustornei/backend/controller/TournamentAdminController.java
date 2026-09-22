package com.olympustornei.backend.controller;

import com.olympustornei.backend.dto.TournamentRequest;
import com.olympustornei.backend.dto.TournamentResponse;
import com.olympustornei.backend.service.TournamentService;
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
@RequestMapping("/api/admin/tournaments")
public class TournamentAdminController {

    private final TournamentService tournamentService;

    public TournamentAdminController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentResponse create(@Valid @RequestBody TournamentRequest request) {
        return tournamentService.create(request);
    }

    @PutMapping("/{id}")
    public TournamentResponse update(@PathVariable Long id, @Valid @RequestBody TournamentRequest request) {
        return tournamentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tournamentService.delete(id);
    }
}
