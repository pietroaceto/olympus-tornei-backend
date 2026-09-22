package com.olympustornei.backend.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BracketSeederTest {

    @Test
    void nextPowerOfTwo_exactPowersReturnThemselves() {
        assertEquals(1, BracketSeeder.nextPowerOfTwo(1));
        assertEquals(2, BracketSeeder.nextPowerOfTwo(2));
        assertEquals(4, BracketSeeder.nextPowerOfTwo(4));
        assertEquals(8, BracketSeeder.nextPowerOfTwo(8));
    }

    @Test
    void nextPowerOfTwo_roundsUpNonPowers() {
        assertEquals(4, BracketSeeder.nextPowerOfTwo(3));
        assertEquals(8, BracketSeeder.nextPowerOfTwo(5));
        assertEquals(8, BracketSeeder.nextPowerOfTwo(6));
        assertEquals(8, BracketSeeder.nextPowerOfTwo(7));
        assertEquals(16, BracketSeeder.nextPowerOfTwo(9));
    }

    @Test
    void seedOrder_knownValuesForSmallBrackets() {
        assertArrayEquals(new int[]{1, 2}, BracketSeeder.seedOrder(2));
        assertArrayEquals(new int[]{1, 4, 2, 3}, BracketSeeder.seedOrder(4));
        assertArrayEquals(new int[]{1, 8, 4, 5, 2, 7, 3, 6}, BracketSeeder.seedOrder(8));
    }

    @Test
    void seedOrder_isPermutationOfOneToSize() {
        for (int size : new int[]{2, 4, 8, 16, 32}) {
            int[] order = BracketSeeder.seedOrder(size);
            assertEquals(size, order.length);
            Set<Integer> seen = new HashSet<>();
            for (int seed : order) {
                assertEquals(true, seed >= 1 && seed <= size, "seed fuori range: " + seed);
                assertEquals(true, seen.add(seed), "seed duplicato: " + seed);
            }
        }
    }

    @Test
    void seedOrder_everyFirstRoundPairSumsToSizePlusOne() {
        // Proprietà classica del seeding standard: in ogni coppia del primo
        // turno la somma dei due seed è sempre size+1 (1+size, 2+(size-1), ...).
        for (int size : new int[]{2, 4, 8, 16}) {
            int[] order = BracketSeeder.seedOrder(size);
            for (int i = 0; i < size; i += 2) {
                assertEquals(size + 1, order[i] + order[i + 1],
                        "la coppia (" + order[i] + "," + order[i + 1] + ") non somma a size+1=" + (size + 1));
            }
        }
    }

    @Test
    void seedOrder_rejectsNonPowerOfTwo() {
        assertThrows(IllegalArgumentException.class, () -> BracketSeeder.seedOrder(3));
        assertThrows(IllegalArgumentException.class, () -> BracketSeeder.seedOrder(6));
        assertThrows(IllegalArgumentException.class, () -> BracketSeeder.seedOrder(0));
    }

    @Test
    void byesAreNeverPairedTogetherInFirstRound_whenQualifiedExceedsHalfOfBracket() {
        // Proprietà chiave per la generazione del tabellone: con
        // bracketSize = nextPowerOfTwo(qualifiedCount), il numero di bye
        // (bracketSize - qualifiedCount) è sempre < bracketSize/2, quindi
        // nessuna coppia del primo turno può avere due bye (cioè due seed
        // entrambi > qualifiedCount).
        for (int qualifiedCount = 2; qualifiedCount <= 20; qualifiedCount++) {
            int bracketSize = BracketSeeder.nextPowerOfTwo(qualifiedCount);
            int[] order = BracketSeeder.seedOrder(bracketSize);
            for (int i = 0; i < bracketSize; i += 2) {
                boolean firstIsBye = order[i] > qualifiedCount;
                boolean secondIsBye = order[i + 1] > qualifiedCount;
                assertEquals(false, firstIsBye && secondIsBye,
                        "con " + qualifiedCount + " qualificate (bracket " + bracketSize
                                + ") la coppia (" + order[i] + "," + order[i + 1] + ") ha due bye");
            }
        }
    }
}
