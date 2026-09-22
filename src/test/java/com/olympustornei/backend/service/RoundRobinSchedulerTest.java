package com.olympustornei.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.olympustornei.backend.service.RoundRobinScheduler.Pairing;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RoundRobinSchedulerTest {

    @Test
    void evenNumberOfTeams_everyTeamPlaysEveryRound_everyPairMeetsOnce() {
        List<Long> teams = List.of(1L, 2L, 3L, 4L);
        List<List<Pairing>> rounds = RoundRobinScheduler.schedule(teams);

        assertEquals(3, rounds.size(), "con 4 squadre servono N-1=3 giornate");

        Set<Set<Long>> allPairs = new HashSet<>();
        for (List<Pairing> round : rounds) {
            assertEquals(2, round.size(), "con 4 squadre ogni giornata ha 2 partite, nessun riposo");
            Set<Long> teamsInRound = new HashSet<>();
            for (Pairing pairing : round) {
                assertTrue(teamsInRound.add(pairing.homeId()), "una squadra non può giocare due partite nella stessa giornata");
                assertTrue(teamsInRound.add(pairing.awayId()), "una squadra non può giocare due partite nella stessa giornata");
                assertTrue(allPairs.add(Set.of(pairing.homeId(), pairing.awayId())), "due squadre non possono incontrarsi due volte nel girone");
            }
            assertEquals(4, teamsInRound.size(), "tutte le squadre devono giocare in ogni giornata");
        }
        assertEquals(6, allPairs.size(), "con 4 squadre ci sono C(4,2)=6 incontri totali");
    }

    @Test
    void oddNumberOfTeams_eachTeamRestsExactlyOnce_everyPairMeetsOnce() {
        List<Long> teams = List.of(1L, 2L, 3L, 4L, 5L);
        List<List<Pairing>> rounds = RoundRobinScheduler.schedule(teams);

        assertEquals(5, rounds.size(), "con 5 squadre servono N=5 giornate (una in riposo a turno)");

        Set<Set<Long>> allPairs = new HashSet<>();
        java.util.Map<Long, Integer> restCount = new java.util.HashMap<>();
        for (Long id : teams) {
            restCount.put(id, 0);
        }

        for (List<Pairing> round : rounds) {
            assertEquals(2, round.size(), "con 5 squadre ogni giornata ha 2 partite e una squadra riposa");
            Set<Long> playing = new HashSet<>();
            for (Pairing pairing : round) {
                assertTrue(playing.add(pairing.homeId()));
                assertTrue(playing.add(pairing.awayId()));
                assertTrue(allPairs.add(Set.of(pairing.homeId(), pairing.awayId())), "due squadre non possono incontrarsi due volte nel girone");
            }
            for (Long id : teams) {
                if (!playing.contains(id)) {
                    restCount.merge(id, 1, Integer::sum);
                }
            }
        }

        assertEquals(10, allPairs.size(), "con 5 squadre ci sono C(5,2)=10 incontri totali");
        for (Long id : teams) {
            assertEquals(1, restCount.get(id), "ogni squadra deve riposare esattamente una volta con 5 squadre");
        }
    }

    @Test
    void fewerThanTwoTeams_throws() {
        assertThrows(IllegalArgumentException.class, () -> RoundRobinScheduler.schedule(List.of(1L)));
    }
}
