package com.olympustornei.backend.controller;

import com.olympustornei.backend.dto.TournamentResponse;
import com.olympustornei.backend.service.TournamentService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping
    public List<TournamentResponse> listAll() {
        return tournamentService.listAll();
    }

    @GetMapping("/{id}")
    public TournamentResponse getById(@PathVariable Long id) {
        return tournamentService.getById(id);
    }
}
