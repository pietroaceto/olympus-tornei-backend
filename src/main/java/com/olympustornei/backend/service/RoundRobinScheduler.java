package com.olympustornei.backend.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Algoritmo del cerchio (circle method) per generare un calendario round-robin
 * a partire da una lista di identificativi di squadra. Puramente funzionale,
 * senza dipendenze da persistenza, per poter essere testato in isolamento.
 */
public final class RoundRobinScheduler {

    private RoundRobinScheduler() {
    }

    public record Pairing(Long homeId, Long awayId) {
    }

    /**
     * Genera il calendario: una lista di round, ciascuno con le coppie home/away
     * di quel turno. Con un numero dispari di squadre viene aggiunto un turno di
     * riposo (bye): la squadra in riposo in quel round semplicemente non compare
     * in nessuna coppia.
     */
    public static List<List<Pairing>> schedule(List<Long> teamIds) {
        if (teamIds.size() < 2) {
            throw new IllegalArgumentException("Servono almeno 2 squadre");
        }

        List<Long> circle = new ArrayList<>(teamIds);
        if (circle.size() % 2 != 0) {
            circle.add(null);
        }
        int n = circle.size();
        int totalRounds = n - 1;

        List<List<Pairing>> rounds = new ArrayList<>();
        for (int roundIndex = 0; roundIndex < totalRounds; roundIndex++) {
            List<Pairing> roundPairings = new ArrayList<>();
            for (int i = 0; i < n / 2; i++) {
                Long t1 = circle.get(i);
                Long t2 = circle.get(n - 1 - i);
                if (t1 == null || t2 == null) {
                    continue;
                }
                boolean swap = i == 0 && roundIndex % 2 == 1;
                roundPairings.add(swap ? new Pairing(t2, t1) : new Pairing(t1, t2));
            }
            rounds.add(roundPairings);
            rotate(circle);
        }
        return rounds;
    }

    private static void rotate(List<Long> circle) {
        int n = circle.size();
        Long last = circle.remove(n - 1);
        circle.add(1, last);
    }
}
