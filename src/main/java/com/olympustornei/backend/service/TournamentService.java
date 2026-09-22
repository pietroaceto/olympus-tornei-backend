package com.olympustornei.backend.service;

import com.olympustornei.backend.domain.Tournament;
import com.olympustornei.backend.domain.TournamentStatus;
import com.olympustornei.backend.dto.TournamentRequest;
import com.olympustornei.backend.dto.TournamentResponse;
import com.olympustornei.backend.repository.TournamentRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class TournamentService {

    private final TournamentRepository tournamentRepository;

    public TournamentService(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    public TournamentResponse create(TournamentRequest request) {
        Tournament tournament = new Tournament();
        tournament.setName(request.name());
        tournament.setSeason(request.season());
        tournament.setStatus(request.status() != null ? request.status() : TournamentStatus.DRAFT);
        tournamentRepository.save(tournament);
        return toResponse(tournament);
    }

    public TournamentResponse update(Long id, TournamentRequest request) {
        Tournament tournament = findEntity(id);
        tournament.setName(request.name());
        tournament.setSeason(request.season());
        if (request.status() != null) {
            tournament.setStatus(request.status());
        }
        return toResponse(tournament);
    }

    public void delete(Long id) {
        if (!tournamentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        tournamentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public TournamentResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<TournamentResponse> listAll() {
        return tournamentRepository.findAll().stream().map(this::toResponse).toList();
    }

    Tournament findEntity(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private TournamentResponse toResponse(Tournament tournament) {
        return new TournamentResponse(
                tournament.getId(),
                tournament.getName(),
                tournament.getSeason(),
                tournament.getStatus().name());
    }
}
