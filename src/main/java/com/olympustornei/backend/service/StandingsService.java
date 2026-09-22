package com.olympustornei.backend.service;

import com.olympustornei.backend.domain.Match;
import com.olympustornei.backend.domain.MatchPhase;
import com.olympustornei.backend.domain.MatchStatus;
import com.olympustornei.backend.domain.SetScore;
import com.olympustornei.backend.domain.SubMatch;
import com.olympustornei.backend.dto.StandingRowResponse;
import com.olympustornei.backend.repository.MatchRepository;
import com.olympustornei.backend.repository.SetScoreRepository;
import com.olympustornei.backend.repository.SubMatchRepository;
import com.olympustornei.backend.repository.TeamRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classifica calcolata on-the-fly dai risultati salvati (mai persistita), per
 * evitare disallineamenti tra classifica e risultati. La logica di calcolo e
 * ordinamento vera e propria è in {@link StandingsCalculator} (pura,
 * testabile senza DB); questo service si occupa solo di caricare i dati da
 * persistenza e tradurli nel formato che il calcolatore si aspetta.
 */
@Service
@Transactional(readOnly = true)
public class StandingsService {

    private final CategoryService categoryService;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final SubMatchRepository subMatchRepository;
    private final SetScoreRepository setScoreRepository;

    public StandingsService(CategoryService categoryService, TeamRepository teamRepository,
                             MatchRepository matchRepository, SubMatchRepository subMatchRepository,
                             SetScoreRepository setScoreRepository) {
        this.categoryService = categoryService;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.subMatchRepository = subMatchRepository;
        this.setScoreRepository = setScoreRepository;
    }

    public List<StandingRowResponse> getStandings(Long categoryId) {
        categoryService.findEntity(categoryId);

        List<StandingsCalculator.TeamInfo> teams = teamRepository.findByCategoryId(categoryId).stream()
                .map(t -> new StandingsCalculator.TeamInfo(t.getId(), t.getName()))
                .toList();

        List<StandingsCalculator.MatchResult> results = matchRepository
                .findByCategoryIdAndPhase(categoryId, MatchPhase.GIRONE).stream()
                .filter(m -> m.getStatus() == MatchStatus.PLAYED)
                .map(this::toMatchResult)
                .toList();

        return StandingsCalculator.compute(teams, results);
    }

    private StandingsCalculator.MatchResult toMatchResult(Match match) {
        int homeSets = 0;
        int awaySets = 0;
        int homeGames = 0;
        int awayGames = 0;
        for (SubMatch subMatch : subMatchRepository.findByMatchIdOrderByOrdineAsc(match.getId())) {
            for (SetScore set : setScoreRepository.findBySubMatchIdOrderBySetNumberAsc(subMatch.getId())) {
                homeGames += set.getHomeGames();
                awayGames += set.getAwayGames();
                if (set.getHomeGames() > set.getAwayGames()) {
                    homeSets++;
                } else if (set.getAwayGames() > set.getHomeGames()) {
                    awaySets++;
                }
            }
        }
        return new StandingsCalculator.MatchResult(
                match.getHomeTeam().getId(), match.getAwayTeam().getId(), match.getResultType(),
                homeSets, awaySets, homeGames, awayGames);
    }
}
