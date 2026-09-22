package com.olympustornei.backend.service;

import com.olympustornei.backend.domain.Category;
import com.olympustornei.backend.domain.CompetitionFormat;
import com.olympustornei.backend.domain.Match;
import com.olympustornei.backend.domain.MatchPhase;
import com.olympustornei.backend.domain.MatchStatus;
import com.olympustornei.backend.domain.MatchdayRound;
import com.olympustornei.backend.domain.Team;
import com.olympustornei.backend.dto.MatchResponse;
import com.olympustornei.backend.dto.RoundResponse;
import com.olympustornei.backend.repository.MatchRepository;
import com.olympustornei.backend.repository.MatchdayRoundRepository;
import com.olympustornei.backend.repository.TeamRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class RoundRobinService {

    private final CategoryService categoryService;
    private final TeamRepository teamRepository;
    private final MatchdayRoundRepository matchdayRoundRepository;
    private final MatchRepository matchRepository;

    public RoundRobinService(CategoryService categoryService, TeamRepository teamRepository,
                              MatchdayRoundRepository matchdayRoundRepository, MatchRepository matchRepository) {
        this.categoryService = categoryService;
        this.teamRepository = teamRepository;
        this.matchdayRoundRepository = matchdayRoundRepository;
        this.matchRepository = matchRepository;
    }

    public List<RoundResponse> generateSchedule(Long categoryId) {
        Category category = categoryService.findEntity(categoryId);
        if (category.getCompetitionFormat() == CompetitionFormat.TABELLONE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Categoria impostata su tabellone diretto: non prevede un girone");
        }
        if (category.isScheduleLocked()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Categoria bloccata: sono già stati inseriti risultati, rigenerare il calendario richiede un reset esplicito");
        }

        List<Team> teams = teamRepository.findByCategoryId(categoryId);
        if (teams.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Servono almeno 2 squadre per generare il calendario");
        }
        Map<Long, Team> teamsById = new HashMap<>();
        for (Team team : teams) {
            teamsById.put(team.getId(), team);
        }

        List<MatchdayRound> existingRounds = matchdayRoundRepository.findByCategoryIdOrderByRoundNumberAsc(categoryId);
        matchdayRoundRepository.deleteAll(existingRounds);
        matchdayRoundRepository.flush();

        List<Long> teamIds = teams.stream().map(Team::getId).toList();
        List<List<RoundRobinScheduler.Pairing>> rounds = RoundRobinScheduler.schedule(teamIds);

        List<RoundResponse> response = new ArrayList<>();
        int roundNumber = 1;
        for (List<RoundRobinScheduler.Pairing> pairings : rounds) {
            MatchdayRound round = new MatchdayRound();
            round.setCategory(category);
            round.setRoundNumber(roundNumber);
            matchdayRoundRepository.save(round);

            List<MatchResponse> roundMatches = new ArrayList<>();
            for (RoundRobinScheduler.Pairing pairing : pairings) {
                Match match = new Match();
                match.setCategory(category);
                match.setPhase(MatchPhase.GIRONE);
                match.setRound(round);
                match.setHomeTeam(teamsById.get(pairing.homeId()));
                match.setAwayTeam(teamsById.get(pairing.awayId()));
                match.setStatus(MatchStatus.SCHEDULED);
                matchRepository.save(match);
                roundMatches.add(toMatchResponse(match));
            }
            response.add(new RoundResponse(roundNumber, roundMatches));
            roundNumber++;
        }

        return response;
    }

    public void resetGirone(Long categoryId) {
        Category category = categoryService.findEntity(categoryId);
        List<MatchdayRound> existingRounds = matchdayRoundRepository.findByCategoryIdOrderByRoundNumberAsc(categoryId);
        matchdayRoundRepository.deleteAll(existingRounds);
        category.setScheduleLocked(false);
    }

    @Transactional(readOnly = true)
    public List<RoundResponse> getSchedule(Long categoryId) {
        categoryService.findEntity(categoryId);
        List<MatchdayRound> rounds = matchdayRoundRepository.findByCategoryIdOrderByRoundNumberAsc(categoryId);
        List<Match> matches = matchRepository.findByCategoryIdAndPhase(categoryId, MatchPhase.GIRONE);

        List<RoundResponse> response = new ArrayList<>();
        for (MatchdayRound round : rounds) {
            List<MatchResponse> roundMatches = matches.stream()
                    .filter(m -> m.getRound() != null && m.getRound().getId().equals(round.getId()))
                    .sorted(Comparator.comparing(Match::getId))
                    .map(this::toMatchResponse)
                    .toList();
            response.add(new RoundResponse(round.getRoundNumber(), roundMatches));
        }
        return response;
    }

    private MatchResponse toMatchResponse(Match match) {
        return new MatchResponse(
                match.getId(),
                match.getHomeTeam().getId(),
                match.getHomeTeam().getName(),
                match.getAwayTeam().getId(),
                match.getAwayTeam().getName(),
                match.getStatus().name());
    }
}
