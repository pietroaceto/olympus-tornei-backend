package com.olympustornei.backend.service;

import com.olympustornei.backend.domain.MatchResultType;
import com.olympustornei.backend.dto.StandingRowResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcolo puro della classifica, senza dipendenze da persistenza, per poter
 * essere testato in isolamento. Punteggio: 3 punti vittoria, 2 punti vittoria
 * al tie-break, 0 punti sconfitta. Spareggio: punti -> differenza set ->
 * differenza game -> scontro diretto (valido solo tra coppie di squadre, dato
 * che in un girone all'italiana singolo c'è un solo incontro possibile tra
 * due squadre); con tre o più squadre in parità su tutti i criteri
 * precedenti si ricade sull'ordine alfabetico come ultimo spareggio
 * deterministico.
 */
public final class StandingsCalculator {

    private StandingsCalculator() {
    }

    public record TeamInfo(Long id, String name) {
    }

    public record MatchResult(Long homeTeamId, Long awayTeamId, MatchResultType resultType,
                               int homeSets, int awaySets, int homeGames, int awayGames) {
    }

    public static List<StandingRowResponse> compute(List<TeamInfo> teams, List<MatchResult> results) {
        Map<Long, TeamStats> statsByTeam = new HashMap<>();
        for (TeamInfo team : teams) {
            statsByTeam.put(team.id(), new TeamStats(team.id(), team.name()));
        }

        Map<Long, Map<Long, Long>> headToHeadWinner = new HashMap<>();

        for (MatchResult result : results) {
            TeamStats home = statsByTeam.get(result.homeTeamId());
            TeamStats away = statsByTeam.get(result.awayTeamId());
            if (home == null || away == null) {
                continue;
            }

            home.played++;
            away.played++;
            home.setsWon += result.homeSets();
            home.setsLost += result.awaySets();
            away.setsWon += result.awaySets();
            away.setsLost += result.homeSets();
            home.gamesWon += result.homeGames();
            home.gamesLost += result.awayGames();
            away.gamesWon += result.awayGames();
            away.gamesLost += result.homeGames();

            boolean homeWins = switch (result.resultType()) {
                case WIN_HOME, WIN_HOME_TB -> true;
                case WIN_AWAY, WIN_AWAY_TB -> false;
            };
            boolean tieBreak = result.resultType() == MatchResultType.WIN_HOME_TB
                    || result.resultType() == MatchResultType.WIN_AWAY_TB;
            int winnerPoints = tieBreak ? 2 : 3;

            if (homeWins) {
                home.won++;
                home.points += winnerPoints;
                away.lost++;
            } else {
                away.won++;
                away.points += winnerPoints;
                home.lost++;
            }

            long winnerId = homeWins ? result.homeTeamId() : result.awayTeamId();
            headToHeadWinner.computeIfAbsent(result.homeTeamId(), k -> new HashMap<>()).put(result.awayTeamId(), winnerId);
            headToHeadWinner.computeIfAbsent(result.awayTeamId(), k -> new HashMap<>()).put(result.homeTeamId(), winnerId);
        }

        List<TeamStats> ordered = new ArrayList<>(statsByTeam.values());
        ordered.sort(StandingsCalculator::compareByStats);
        applyHeadToHeadTieBreak(ordered, headToHeadWinner);

        List<StandingRowResponse> response = new ArrayList<>();
        int position = 1;
        for (TeamStats stats : ordered) {
            response.add(new StandingRowResponse(
                    position++,
                    stats.teamId,
                    stats.teamName,
                    stats.played,
                    stats.won,
                    stats.lost,
                    stats.points,
                    stats.setsWon,
                    stats.setsLost,
                    stats.setsWon - stats.setsLost,
                    stats.gamesWon,
                    stats.gamesLost,
                    stats.gamesWon - stats.gamesLost));
        }
        return response;
    }

    /**
     * Ordinamento base su punti/differenza set/differenza game/nome: un vero
     * ordine totale, sempre coerente (a differenza dello scontro diretto, che
     * non è transitivo su più di due squadre e va applicato a parte).
     */
    private static int compareByStats(TeamStats a, TeamStats b) {
        int byPoints = Integer.compare(b.points, a.points);
        if (byPoints != 0) {
            return byPoints;
        }
        int bySetDiff = Integer.compare(b.setsWon - b.setsLost, a.setsWon - a.setsLost);
        if (bySetDiff != 0) {
            return bySetDiff;
        }
        int byGameDiff = Integer.compare(b.gamesWon - b.gamesLost, a.gamesWon - a.gamesLost);
        if (byGameDiff != 0) {
            return byGameDiff;
        }
        return a.teamName.compareToIgnoreCase(b.teamName);
    }

    /**
     * Applica lo scontro diretto solo quando esattamente due squadre risultano
     * in parità su punti/differenza set/differenza game (gruppo adiacente
     * dopo l'ordinamento base): se la seconda ha battuto direttamente la
     * prima, le scambia. Con tre o più squadre in parità lo scontro diretto
     * non è un criterio ben definito (non transitivo), quindi si lascia
     * l'ordinamento alfabetico già applicato da compareByStats.
     */
    private static void applyHeadToHeadTieBreak(List<TeamStats> ordered, Map<Long, Map<Long, Long>> headToHeadWinner) {
        int i = 0;
        while (i < ordered.size()) {
            int j = i + 1;
            while (j < ordered.size() && sameStats(ordered.get(i), ordered.get(j))) {
                j++;
            }
            int groupSize = j - i;
            if (groupSize == 2) {
                TeamStats first = ordered.get(i);
                TeamStats second = ordered.get(i + 1);
                Long winner = headToHeadWinner.getOrDefault(first.teamId, Map.of()).get(second.teamId);
                if (winner != null && winner == second.teamId) {
                    ordered.set(i, second);
                    ordered.set(i + 1, first);
                }
            }
            i = j;
        }
    }

    private static boolean sameStats(TeamStats a, TeamStats b) {
        return a.points == b.points
                && (a.setsWon - a.setsLost) == (b.setsWon - b.setsLost)
                && (a.gamesWon - a.gamesLost) == (b.gamesWon - b.gamesLost);
    }

    private static final class TeamStats {
        final long teamId;
        final String teamName;
        int played;
        int won;
        int lost;
        int points;
        int setsWon;
        int setsLost;
        int gamesWon;
        int gamesLost;

        TeamStats(long teamId, String teamName) {
            this.teamId = teamId;
            this.teamName = teamName;
        }
    }
}
