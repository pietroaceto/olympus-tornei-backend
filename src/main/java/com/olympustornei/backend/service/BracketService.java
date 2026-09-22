package com.olympustornei.backend.service;

import com.olympustornei.backend.domain.Category;
import com.olympustornei.backend.domain.CategoryPhase;
import com.olympustornei.backend.domain.Match;
import com.olympustornei.backend.domain.MatchPhase;
import com.olympustornei.backend.domain.MatchStatus;
import com.olympustornei.backend.domain.Team;
import com.olympustornei.backend.dto.BracketMatchResponse;
import com.olympustornei.backend.dto.BracketResponse;
import com.olympustornei.backend.dto.BracketRoundResponse;
import com.olympustornei.backend.dto.StandingRowResponse;
import com.olympustornei.backend.repository.MatchRepository;
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

/**
 * Generazione e lettura del tabellone a eliminazione diretta. Il seeding
 * (algoritmo puro) è in {@link BracketSeeder}. Solo il primo turno può avere
 * dei "bye" (squadra che passa il turno senza giocare, quando il numero di
 * qualificate non è una potenza di 2): dal secondo turno in poi ogni
 * accoppiamento corrisponde sempre a un match reale da giocare, anche se al
 * momento della generazione una o entrambe le squadre non sono ancora note
 * (in attesa del risultato del match che alimenta quello slot) — per questo i
 * match dei round successivi al primo vengono creati fin da subito, con le
 * squadre valorizzate mano a mano che si rendono note.
 */
@Service
@Transactional
public class BracketService {

    private final CategoryService categoryService;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final StandingsService standingsService;

    public BracketService(CategoryService categoryService, TeamRepository teamRepository,
                           MatchRepository matchRepository, StandingsService standingsService) {
        this.categoryService = categoryService;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.standingsService = standingsService;
    }

    public BracketResponse generateBracket(Long categoryId, int qualifiedCount) {
        Category category = categoryService.findEntity(categoryId);
        if (category.getPhase() != CategoryPhase.GIRONE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Il tabellone è già stato generato (o la categoria non è in fase girone): serve un reset esplicito");
        }

        List<Team> teams = teamRepository.findByCategoryId(categoryId);
        if (qualifiedCount < 2 || qualifiedCount > teams.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Numero di squadre qualificate non valido: deve essere tra 2 e " + teams.size());
        }

        Map<Long, Team> teamsById = new HashMap<>();
        for (Team team : teams) {
            teamsById.put(team.getId(), team);
        }

        List<StandingRowResponse> standings = standingsService.getStandings(categoryId);
        List<Team> seeds = standings.stream()
                .limit(qualifiedCount)
                .map(row -> teamsById.get(row.teamId()))
                .toList();

        int bracketSize = BracketSeeder.nextPowerOfTwo(qualifiedCount);
        int totalRounds = Integer.numberOfTrailingZeros(bracketSize);
        int[] seedOrder = BracketSeeder.seedOrder(bracketSize);

        Team[] slots = new Team[bracketSize];
        for (int i = 0; i < bracketSize; i++) {
            int seed = seedOrder[i];
            slots[i] = seed <= qualifiedCount ? seeds.get(seed - 1) : null;
        }

        category.setPhase(CategoryPhase.TABELLONE);
        category.setBracketTotalRounds(totalRounds);

        // Round 0: crea un match per ogni coppia con entrambe le squadre note;
        // le coppie con un solo lato noto sono bye e la squadra avanza subito,
        // senza creare un match, allo slot corrispondente del round 1.
        int round0Pairs = bracketSize / 2;
        Team[] outcomes = new Team[round0Pairs];
        for (int p = 0; p < round0Pairs; p++) {
            Team a = slots[2 * p];
            Team b = slots[2 * p + 1];
            if (a != null && b != null) {
                createMatch(category, 0, p, a, b);
                outcomes[p] = null;
            } else {
                outcomes[p] = (a != null) ? a : b;
            }
        }

        // Round 1 in poi: sempre match reali (mai più bye strutturali), creati
        // fin da subito con le squadre note al momento (eventualmente nessuna).
        for (int round = 1; round < totalRounds; round++) {
            int pairs = outcomes.length / 2;
            Team[] nextOutcomes = new Team[pairs];
            for (int p = 0; p < pairs; p++) {
                Team home = outcomes[2 * p];
                Team away = outcomes[2 * p + 1];
                createMatch(category, round, p, home, away);
                nextOutcomes[p] = null;
            }
            outcomes = nextOutcomes;
        }

        return getBracket(categoryId);
    }

    public void resetBracket(Long categoryId) {
        Category category = categoryService.findEntity(categoryId);
        List<Match> matches = matchRepository.findByCategoryIdAndPhase(categoryId, MatchPhase.TABELLONE);
        matchRepository.deleteAll(matches);
        category.setPhase(CategoryPhase.GIRONE);
        category.setBracketTotalRounds(null);
    }

    @Transactional(readOnly = true)
    public BracketResponse getBracket(Long categoryId) {
        Category category = categoryService.findEntity(categoryId);
        List<Match> matches = matchRepository.findByCategoryIdAndPhase(categoryId, MatchPhase.TABELLONE);

        Map<Integer, List<Match>> byRound = new HashMap<>();
        for (Match match : matches) {
            byRound.computeIfAbsent(match.getBracketRoundIndex(), k -> new ArrayList<>()).add(match);
        }

        List<BracketRoundResponse> rounds = new ArrayList<>();
        byRound.keySet().stream().sorted().forEach(roundIndex -> {
            List<BracketMatchResponse> roundMatches = byRound.get(roundIndex).stream()
                    .sorted(Comparator.comparing(Match::getBracketSlot))
                    .map(this::toBracketMatchResponse)
                    .toList();
            rounds.add(new BracketRoundResponse(roundIndex, roundMatches));
        });

        return new BracketResponse(category.getBracketTotalRounds(), rounds);
    }

    private void createMatch(Category category, int round, int slot, Team home, Team away) {
        Match match = new Match();
        match.setCategory(category);
        match.setPhase(MatchPhase.TABELLONE);
        match.setBracketRoundIndex(round);
        match.setBracketSlot(slot);
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        match.setStatus(MatchStatus.SCHEDULED);
        matchRepository.save(match);
    }

    private BracketMatchResponse toBracketMatchResponse(Match match) {
        return new BracketMatchResponse(
                match.getId(),
                match.getBracketSlot(),
                match.getHomeTeam() != null ? match.getHomeTeam().getId() : null,
                match.getHomeTeam() != null ? match.getHomeTeam().getName() : null,
                match.getAwayTeam() != null ? match.getAwayTeam().getId() : null,
                match.getAwayTeam() != null ? match.getAwayTeam().getName() : null,
                match.getStatus().name(),
                match.getResultType() != null ? match.getResultType().name() : null,
                match.getWinnerTeam() != null ? match.getWinnerTeam().getId() : null);
    }
}
