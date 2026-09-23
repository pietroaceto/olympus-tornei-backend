package com.olympustornei.backend.service;

import com.olympustornei.backend.domain.Category;
import com.olympustornei.backend.domain.CategoryPhase;
import com.olympustornei.backend.domain.CompetitionFormat;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 *
 * <p>Esistono tre modi di arrivare al primo turno del tabellone, a seconda
 * del {@link CompetitionFormat} della categoria: {@link #generateBracket}
 * (categorie GIRONE, seeding dalla classifica — usato sia dal trigger
 * automatico a fine girone in {@code MatchResultService}, sia da un'eventuale
 * rigenerazione manuale), {@link #generateRandomBracket} (categorie
 * TABELLONE dirette, accoppiamenti casuali) e {@link #generateManualBracket}
 * (categorie TABELLONE dirette, accoppiamenti scelti a mano dall'admin). I
 * primi due riusano lo stesso algoritmo di seeding di {@link BracketSeeder}
 * (garantisce che nessuna coppia del primo turno sia bye+bye); il terzo usa
 * gli slot esattamente come forniti dall'admin.
 */
@Service
@Transactional
public class BracketService {

    private final CategoryService categoryService;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final StandingsService standingsService;
    private final MatchScoreService matchScoreService;

    public BracketService(CategoryService categoryService, TeamRepository teamRepository,
                           MatchRepository matchRepository, StandingsService standingsService,
                           MatchScoreService matchScoreService) {
        this.categoryService = categoryService;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.standingsService = standingsService;
        this.matchScoreService = matchScoreService;
    }

    public BracketResponse generateBracket(Long categoryId, int qualifiedCount) {
        Category category = categoryService.findEntity(categoryId);
        if (category.getCompetitionFormat() != CompetitionFormat.GIRONE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Questa categoria è impostata su tabellone diretto: usa la generazione automatica o manuale");
        }
        requireGironePhase(category);

        List<Team> teams = teamRepository.findByCategoryId(categoryId);
        if (qualifiedCount < 2 || qualifiedCount > teams.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Numero di squadre qualificate non valido: deve essere tra 2 e " + teams.size());
        }

        Map<Long, Team> teamsById = buildTeamsById(teams);
        List<StandingRowResponse> standings = standingsService.getStandings(categoryId);
        List<Team> seeds = standings.stream()
                .limit(qualifiedCount)
                .map(row -> teamsById.get(row.teamId()))
                .toList();

        return buildFromSlots(category, buildSeededSlots(seeds));
    }

    /**
     * Chiamato da {@code MatchResultService} quando l'ultimo risultato del
     * girone viene inserito: genera subito il tabellone con tutte le squadre
     * (nessun taglio) — criterio provvisorio, da rivedere quando si
     * decideranno le regole di qualificazione definitive.
     */
    public BracketResponse generateAutoBracketFromGirone(Long categoryId) {
        int teamCount = teamRepository.findByCategoryId(categoryId).size();
        return generateBracket(categoryId, teamCount);
    }

    public BracketResponse generateRandomBracket(Long categoryId) {
        Category category = categoryService.findEntity(categoryId);
        requireTabelloneDirettoFormat(category);
        requireGironePhase(category);

        List<Team> teams = new ArrayList<>(teamRepository.findByCategoryId(categoryId));
        if (teams.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Servono almeno 2 squadre per generare il tabellone");
        }
        Collections.shuffle(teams);

        return buildFromSlots(category, buildSeededSlots(teams));
    }

    public BracketResponse generateManualBracket(Long categoryId, List<Long> slotTeamIds) {
        Category category = categoryService.findEntity(categoryId);
        requireTabelloneDirettoFormat(category);
        requireGironePhase(category);

        List<Team> teams = teamRepository.findByCategoryId(categoryId);
        if (teams.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Servono almeno 2 squadre per generare il tabellone");
        }
        int expectedSize = BracketSeeder.nextPowerOfTwo(teams.size());
        if (slotTeamIds.size() != expectedSize) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Il numero di slot deve essere " + expectedSize + " per " + teams.size() + " squadre");
        }

        Map<Long, Team> teamsById = buildTeamsById(teams);
        Set<Long> assigned = new HashSet<>();
        Team[] slots = new Team[expectedSize];
        for (int i = 0; i < expectedSize; i++) {
            Long teamId = slotTeamIds.get(i);
            if (teamId == null) {
                continue;
            }
            Team team = teamsById.get(teamId);
            if (team == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Squadra " + teamId + " non trovata in questa categoria");
            }
            if (!assigned.add(teamId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La squadra \"" + team.getName() + "\" è assegnata a più di uno slot");
            }
            slots[i] = team;
        }
        if (assigned.size() != teams.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ogni squadra deve essere assegnata a esattamente uno slot");
        }
        for (int p = 0; p < expectedSize / 2; p++) {
            if (slots[2 * p] == null && slots[2 * p + 1] == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Non è possibile lasciare due BYE nello stesso accoppiamento (slot " + (2 * p + 1) + "-" + (2 * p + 2) + ")");
            }
        }

        return buildFromSlots(category, slots);
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

    private void requireGironePhase(Category category) {
        if (category.getPhase() != CategoryPhase.GIRONE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Il tabellone è già stato generato (o la categoria non è in fase girone): serve un reset esplicito");
        }
    }

    private void requireTabelloneDirettoFormat(Category category) {
        if (category.getCompetitionFormat() != CompetitionFormat.TABELLONE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Questa categoria usa il girone: il tabellone si genera automaticamente al termine del girone");
        }
    }

    /**
     * Applica l'algoritmo di seeding di {@link BracketSeeder} a una lista di
     * squadre già ordinata (per classifica, o mescolata a caso): mappa ogni
     * posizione della lista a uno slot del tabellone garantendo che nessuna
     * coppia del primo turno sia bye+bye.
     */
    private Team[] buildSeededSlots(List<Team> orderedTeams) {
        int qualifiedCount = orderedTeams.size();
        int bracketSize = BracketSeeder.nextPowerOfTwo(qualifiedCount);
        int[] seedOrder = BracketSeeder.seedOrder(bracketSize);

        Team[] slots = new Team[bracketSize];
        for (int i = 0; i < bracketSize; i++) {
            int seed = seedOrder[i];
            slots[i] = seed <= qualifiedCount ? orderedTeams.get(seed - 1) : null;
        }
        return slots;
    }

    /**
     * Crea i match di tutti i round a partire dagli slot del primo turno,
     * già risolti (da seeding o da assegnazione manuale). Round 0: crea un
     * match per ogni coppia con entrambe le squadre note; le coppie con un
     * solo lato noto sono bye e la squadra avanza subito, senza creare un
     * match, allo slot corrispondente del round 1. Round 1 in poi: sempre
     * match reali (mai più bye strutturali), creati fin da subito con le
     * squadre note al momento (eventualmente nessuna).
     */
    private BracketResponse buildFromSlots(Category category, Team[] slots) {
        int bracketSize = slots.length;
        int totalRounds = Integer.numberOfTrailingZeros(bracketSize);

        category.setPhase(CategoryPhase.TABELLONE);
        category.setBracketTotalRounds(totalRounds);

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

        return getBracket(category.getId());
    }

    private Map<Long, Team> buildTeamsById(List<Team> teams) {
        Map<Long, Team> teamsById = new HashMap<>();
        for (Team team : teams) {
            teamsById.put(team.getId(), team);
        }
        return teamsById;
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
                match.getWinnerTeam() != null ? match.getWinnerTeam().getId() : null,
                matchScoreService.buildSubMatchScores(match));
    }
}
