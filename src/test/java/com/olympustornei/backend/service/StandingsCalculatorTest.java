package com.olympustornei.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.olympustornei.backend.domain.MatchResultType;
import com.olympustornei.backend.dto.StandingRowResponse;
import com.olympustornei.backend.service.StandingsCalculator.MatchResult;
import com.olympustornei.backend.service.StandingsCalculator.TeamInfo;
import java.util.List;
import org.junit.jupiter.api.Test;

class StandingsCalculatorTest {

    private static final TeamInfo A = new TeamInfo(1L, "Alpha");
    private static final TeamInfo B = new TeamInfo(2L, "Beta");
    private static final TeamInfo C = new TeamInfo(3L, "Gamma");
    private static final TeamInfo D = new TeamInfo(4L, "Delta");

    @Test
    void teamWithNoMatchesHasZeroedStats() {
        List<StandingRowResponse> standings = StandingsCalculator.compute(List.of(A, B), List.of());

        assertEquals(2, standings.size());
        for (StandingRowResponse row : standings) {
            assertEquals(0, row.played());
            assertEquals(0, row.points());
        }
    }

    @Test
    void straightWinEarnsThreePoints_tieBreakWinEarnsTwo() {
        // A batte B 2 set a 0 (vittoria "normale"), B batte C al tie-break.
        List<MatchResult> results = List.of(
                new MatchResult(A.id(), B.id(), MatchResultType.WIN_HOME, 2, 0, 12, 6),
                new MatchResult(B.id(), C.id(), MatchResultType.WIN_HOME_TB, 2, 1, 15, 12)
        );

        List<StandingRowResponse> standings = StandingsCalculator.compute(List.of(A, B, C), results);

        StandingRowResponse aRow = findTeam(standings, A.id());
        StandingRowResponse bRow = findTeam(standings, B.id());
        StandingRowResponse cRow = findTeam(standings, C.id());

        assertEquals(3, aRow.points(), "vittoria normale vale 3 punti");
        assertEquals(2, bRow.points(), "vittoria al tie-break vale 2 punti, sommata alla sconfitta da 0 contro A");
        assertEquals(0, cRow.points(), "sconfitta vale 0 punti");
    }

    @Test
    void ordersByPointsThenSetDiffThenGameDiff() {
        // A e B arrivano entrambe a 3 punti (una vittoria ciascuna contro C),
        // ma A ha una differenza set/game migliore.
        List<MatchResult> results = List.of(
                new MatchResult(A.id(), C.id(), MatchResultType.WIN_HOME, 2, 0, 12, 4),
                new MatchResult(B.id(), C.id(), MatchResultType.WIN_HOME, 2, 1, 13, 11)
        );

        List<StandingRowResponse> standings = StandingsCalculator.compute(List.of(A, B, C), results);

        assertEquals(A.id(), standings.get(0).teamId(), "A prima per differenza set/game migliore a parità di punti");
        assertEquals(1, standings.get(0).position());
        assertEquals(B.id(), standings.get(1).teamId());
        assertEquals(C.id(), standings.get(2).teamId(), "C ultima, nessuna vittoria");
    }

    @Test
    void headToHeadBreaksTieWhenPointsAndDiffsAreEqual() {
        // A batte B, B batte C, C batte A: tutte finiscono 2-1 sets e 3 punti a testa
        // (un girone perfettamente in parità), quindi differenza set e game sono
        // identiche per tutti. In questo scenario a 3 non c'è un vero "scontro
        // diretto" risolutivo (ognuno ha vinto e perso una volta), quindi il
        // fallback alfabetico deve applicarsi in modo deterministico.
        List<MatchResult> results = List.of(
                new MatchResult(A.id(), B.id(), MatchResultType.WIN_HOME, 2, 1, 13, 11),
                new MatchResult(B.id(), C.id(), MatchResultType.WIN_HOME, 2, 1, 13, 11),
                new MatchResult(C.id(), A.id(), MatchResultType.WIN_HOME, 2, 1, 13, 11)
        );

        List<StandingRowResponse> standings = StandingsCalculator.compute(List.of(A, B, C), results);

        for (StandingRowResponse row : standings) {
            assertEquals(3, row.points());
            assertEquals(0, row.setDiff());
        }
        assertEquals("Alpha", standings.get(0).teamName(), "fallback alfabetico deterministico a tre in parità totale");
        assertEquals("Beta", standings.get(1).teamName());
        assertEquals("Gamma", standings.get(2).teamName());
    }

    @Test
    void headToHeadBreaksTieBetweenExactlyTwoTeams() {
        // A batte B in casa (3 punti, set 2-0, game 12-6), poi perde da D con
        // lo stesso margine invertito (0 punti, set 0-2, game 6-12): il saldo
        // di A torna a 0/0 di differenza con 3 punti totali. B batte C
        // arrivando anch'essa a 3 punti con differenza 0/0: A e B sono quindi
        // in perfetta parità su punti/differenza set/differenza game, ma A ha
        // battuto B nello scontro diretto e deve precederla in classifica. D,
        // pur fermo a 3 punti come A e B, ha una differenza migliore (+2/+6)
        // e li precede entrambi; C è ultima a 0 punti.
        List<MatchResult> results = List.of(
                new MatchResult(A.id(), B.id(), MatchResultType.WIN_HOME, 2, 0, 12, 6),
                new MatchResult(A.id(), D.id(), MatchResultType.WIN_AWAY, 0, 2, 6, 12),
                new MatchResult(B.id(), C.id(), MatchResultType.WIN_HOME, 2, 0, 6, 0)
        );

        List<StandingRowResponse> standings = StandingsCalculator.compute(List.of(A, B, C, D), results);

        StandingRowResponse aRow = findTeam(standings, A.id());
        StandingRowResponse bRow = findTeam(standings, B.id());
        assertEquals(3, aRow.points());
        assertEquals(0, aRow.setDiff());
        assertEquals(0, aRow.gameDiff());
        assertEquals(3, bRow.points());
        assertEquals(0, bRow.setDiff());
        assertEquals(0, bRow.gameDiff());

        assertEquals(D.id(), standings.get(0).teamId(), "D precede a parità di punti grazie a una differenza migliore");
        assertEquals(A.id(), standings.get(1).teamId(), "a parità totale con B, A precede per scontro diretto diretto vinto");
        assertEquals(B.id(), standings.get(2).teamId());
        assertEquals(C.id(), standings.get(3).teamId(), "C ultima con 0 punti");
    }

    private static StandingRowResponse findTeam(List<StandingRowResponse> standings, Long teamId) {
        return standings.stream().filter(r -> r.teamId().equals(teamId)).findFirst().orElseThrow();
    }
}
