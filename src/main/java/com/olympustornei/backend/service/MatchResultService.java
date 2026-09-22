package com.olympustornei.backend.service;

import com.olympustornei.backend.domain.Category;
import com.olympustornei.backend.domain.CategoryPhase;
import com.olympustornei.backend.domain.Match;
import com.olympustornei.backend.domain.MatchPhase;
import com.olympustornei.backend.domain.MatchResultType;
import com.olympustornei.backend.domain.MatchStatus;
import com.olympustornei.backend.domain.Player;
import com.olympustornei.backend.domain.SetScore;
import com.olympustornei.backend.domain.SubMatch;
import com.olympustornei.backend.domain.Team;
import com.olympustornei.backend.dto.MatchDetailResponse;
import com.olympustornei.backend.dto.MatchResultRequest;
import com.olympustornei.backend.dto.PlayerResponse;
import com.olympustornei.backend.dto.SetScoreRequest;
import com.olympustornei.backend.dto.SetScoreResponse;
import com.olympustornei.backend.dto.SubMatchRequest;
import com.olympustornei.backend.dto.SubMatchResponse;
import com.olympustornei.backend.repository.MatchRepository;
import com.olympustornei.backend.repository.PlayerRepository;
import com.olympustornei.backend.repository.SetScoreRepository;
import com.olympustornei.backend.repository.SubMatchRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MatchResultService {

    private final MatchRepository matchRepository;
    private final SubMatchRepository subMatchRepository;
    private final SetScoreRepository setScoreRepository;
    private final PlayerRepository playerRepository;

    public MatchResultService(MatchRepository matchRepository, SubMatchRepository subMatchRepository,
                               SetScoreRepository setScoreRepository, PlayerRepository playerRepository) {
        this.matchRepository = matchRepository;
        this.subMatchRepository = subMatchRepository;
        this.setScoreRepository = setScoreRepository;
        this.playerRepository = playerRepository;
    }

    public MatchDetailResponse submitResult(Long matchId, MatchResultRequest request) {
        Match match = findEntity(matchId);
        Category category = match.getCategory();

        if (match.getHomeTeam() == null || match.getAwayTeam() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Il match non è ancora completo: una delle due squadre non è stata determinata");
        }

        if (request.subMatches().size() != category.getSubMatchesCount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Numero di sotto-partite non valido: la categoria richiede " + category.getSubMatchesCount());
        }

        List<SubMatch> existing = subMatchRepository.findByMatchIdOrderByOrdineAsc(matchId);
        subMatchRepository.deleteAll(existing);
        subMatchRepository.flush();

        int ordine = 1;
        for (SubMatchRequest subMatchRequest : request.subMatches()) {
            Player homePlayer1 = requirePlayerOfTeam(subMatchRequest.homePlayer1Id(), match.getHomeTeam());
            Player homePlayer2 = requirePlayerOfTeam(subMatchRequest.homePlayer2Id(), match.getHomeTeam());
            Player awayPlayer1 = requirePlayerOfTeam(subMatchRequest.awayPlayer1Id(), match.getAwayTeam());
            Player awayPlayer2 = requirePlayerOfTeam(subMatchRequest.awayPlayer2Id(), match.getAwayTeam());

            SubMatch subMatch = new SubMatch();
            subMatch.setMatch(match);
            subMatch.setOrdine(ordine++);
            subMatch.setHomePlayer1(homePlayer1);
            subMatch.setHomePlayer2(homePlayer2);
            subMatch.setAwayPlayer1(awayPlayer1);
            subMatch.setAwayPlayer2(awayPlayer2);
            subMatchRepository.save(subMatch);

            for (SetScoreRequest setRequest : subMatchRequest.sets()) {
                SetScore setScore = new SetScore();
                setScore.setSubMatch(subMatch);
                setScore.setSetNumber(setRequest.setNumber());
                setScore.setHomeGames(setRequest.homeGames());
                setScore.setAwayGames(setRequest.awayGames());
                setScoreRepository.save(setScore);
            }
        }

        Team winner = isHomeResult(request.resultType()) ? match.getHomeTeam() : match.getAwayTeam();
        match.setStatus(MatchStatus.PLAYED);
        match.setResultType(request.resultType());
        match.setWinnerTeam(winner);

        if (match.getPhase() == MatchPhase.GIRONE) {
            if (!category.isScheduleLocked()) {
                category.setScheduleLocked(true);
            }
        } else {
            advanceWinner(category, match, winner);
        }

        return toDetailResponse(match);
    }

    /**
     * Propaga il vincitore di un match di fase TABELLONE allo slot del round
     * successivo (creato in anticipo da {@link BracketService} alla
     * generazione del tabellone). Se il match appena giocato era la finale,
     * la categoria passa a CONCLUSA.
     */
    private void advanceWinner(Category category, Match match, Team winner) {
        int nextRound = match.getBracketRoundIndex() + 1;
        if (category.getBracketTotalRounds() == null || nextRound >= category.getBracketTotalRounds()) {
            category.setPhase(CategoryPhase.CONCLUSA);
            return;
        }
        int nextSlot = match.getBracketSlot() / 2;
        boolean isHomeSide = match.getBracketSlot() % 2 == 0;
        Match nextMatch = matchRepository
                .findByCategoryIdAndPhaseAndBracketRoundIndexAndBracketSlot(
                        category.getId(), MatchPhase.TABELLONE, nextRound, nextSlot)
                .orElseThrow(() -> new IllegalStateException(
                        "Match del round successivo non trovato: dovrebbe essere stato pre-creato alla generazione del tabellone"));
        if (isHomeSide) {
            nextMatch.setHomeTeam(winner);
        } else {
            nextMatch.setAwayTeam(winner);
        }
    }

    @Transactional(readOnly = true)
    public MatchDetailResponse getById(Long matchId) {
        return toDetailResponse(findEntity(matchId));
    }

    private boolean isHomeResult(MatchResultType resultType) {
        return resultType == MatchResultType.WIN_HOME || resultType == MatchResultType.WIN_HOME_TB;
    }

    private Player requirePlayerOfTeam(Long playerId, Team team) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Giocatore " + playerId + " non trovato"));
        if (!player.getTeam().getId().equals(team.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Il giocatore " + playerId + " non appartiene alla squadra " + team.getName());
        }
        return player;
    }

    private Match findEntity(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private MatchDetailResponse toDetailResponse(Match match) {
        List<SubMatch> subMatches = subMatchRepository.findByMatchIdOrderByOrdineAsc(match.getId());
        List<SubMatchResponse> subMatchResponses = new ArrayList<>();
        int homeSubMatchesWon = 0;
        int awaySubMatchesWon = 0;

        for (SubMatch subMatch : subMatches) {
            List<SetScore> sets = setScoreRepository.findBySubMatchIdOrderBySetNumberAsc(subMatch.getId());
            List<SetScoreResponse> setResponses = sets.stream()
                    .map(s -> new SetScoreResponse(s.getSetNumber(), s.getHomeGames(), s.getAwayGames()))
                    .toList();

            int homeSets = 0;
            int awaySets = 0;
            for (SetScore set : sets) {
                if (set.getHomeGames() > set.getAwayGames()) {
                    homeSets++;
                } else if (set.getAwayGames() > set.getHomeGames()) {
                    awaySets++;
                }
            }
            String setsWonBy = homeSets > awaySets ? "HOME" : (awaySets > homeSets ? "AWAY" : "PARITA");
            if ("HOME".equals(setsWonBy)) {
                homeSubMatchesWon++;
            } else if ("AWAY".equals(setsWonBy)) {
                awaySubMatchesWon++;
            }

            subMatchResponses.add(new SubMatchResponse(
                    subMatch.getId(),
                    subMatch.getOrdine(),
                    toPlayerResponse(subMatch.getHomePlayer1()),
                    toPlayerResponse(subMatch.getHomePlayer2()),
                    toPlayerResponse(subMatch.getAwayPlayer1()),
                    toPlayerResponse(subMatch.getAwayPlayer2()),
                    setResponses,
                    setsWonBy));
        }

        String suggestedWinner = homeSubMatchesWon > awaySubMatchesWon ? "HOME"
                : (awaySubMatchesWon > homeSubMatchesWon ? "AWAY" : "PARITA");

        return new MatchDetailResponse(
                match.getId(),
                match.getCategory().getId(),
                match.getPhase().name(),
                match.getHomeTeam() != null ? match.getHomeTeam().getId() : null,
                match.getHomeTeam() != null ? match.getHomeTeam().getName() : null,
                match.getAwayTeam() != null ? match.getAwayTeam().getId() : null,
                match.getAwayTeam() != null ? match.getAwayTeam().getName() : null,
                match.getStatus().name(),
                match.getResultType() != null ? match.getResultType().name() : null,
                match.getWinnerTeam() != null ? match.getWinnerTeam().getId() : null,
                subMatchResponses.isEmpty() ? null : suggestedWinner,
                subMatchResponses);
    }

    private PlayerResponse toPlayerResponse(Player player) {
        return player == null ? null : new PlayerResponse(player.getId(), player.getName());
    }
}
