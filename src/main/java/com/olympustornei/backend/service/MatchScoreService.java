package com.olympustornei.backend.service;

import com.olympustornei.backend.domain.Match;
import com.olympustornei.backend.domain.MatchStatus;
import com.olympustornei.backend.domain.SetScore;
import com.olympustornei.backend.domain.SubMatch;
import com.olympustornei.backend.dto.MatchSubMatchScoreResponse;
import com.olympustornei.backend.dto.SetScoreResponse;
import com.olympustornei.backend.repository.SetScoreRepository;
import com.olympustornei.backend.repository.SubMatchRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Riepilogo dei punteggi (sotto-partite/set) di un match, riusato sia dal
 * calendario del girone sia dal tabellone: entrambi mostrano lo stesso
 * dettaglio compatto senza dover caricare l'intero {@code MatchDetailResponse}.
 */
@Service
public class MatchScoreService {

    private final SubMatchRepository subMatchRepository;
    private final SetScoreRepository setScoreRepository;

    public MatchScoreService(SubMatchRepository subMatchRepository, SetScoreRepository setScoreRepository) {
        this.subMatchRepository = subMatchRepository;
        this.setScoreRepository = setScoreRepository;
    }

    public List<MatchSubMatchScoreResponse> buildSubMatchScores(Match match) {
        if (match.getStatus() != MatchStatus.PLAYED) {
            return List.of();
        }
        List<SubMatch> subMatches = subMatchRepository.findByMatchIdOrderByOrdineAsc(match.getId());
        List<MatchSubMatchScoreResponse> result = new ArrayList<>();
        for (SubMatch subMatch : subMatches) {
            List<SetScore> sets = setScoreRepository.findBySubMatchIdOrderBySetNumberAsc(subMatch.getId());
            List<SetScoreResponse> setResponses = sets.stream()
                    .map(s -> new SetScoreResponse(s.getSetNumber(), s.getHomeGames(), s.getAwayGames()))
                    .toList();
            if (!setResponses.isEmpty()) {
                result.add(new MatchSubMatchScoreResponse(subMatch.getOrdine(), setResponses));
            }
        }
        return result;
    }
}
