package com.olympustornei.backend.service;

/**
 * Seeding standard da torneo tennistico, senza dipendenze da persistenza,
 * per poter essere testato in isolamento.
 */
public final class BracketSeeder {

    private BracketSeeder() {
    }

    /**
     * La più piccola potenza di 2 maggiore o uguale a n.
     */
    public static int nextPowerOfTwo(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("n deve essere >= 1");
        }
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }

    /**
     * Ordine di seeding standard per un tabellone di dimensione {@code size}
     * (deve essere una potenza di 2): l'elemento in posizione i (0-indicizzata)
     * è il numero di testa di serie (1-indicizzato) assegnato a quello slot.
     * Con size=4 restituisce [1,4,2,3] (round 1: 1 vs 4, 2 vs 3); con size=8
     * restituisce [1,8,4,5,2,7,3,6] (round 1: 1v8, 4v5, 2v7, 3v6) — lo schema
     * classico che garantisce alle teste di serie migliori di incontrarsi il
     * più tardi possibile ed è anche lo schema con cui, in presenza di bye
     * (squadre qualificate in numero inferiore alla dimensione del
     * tabellone), i bye finiscono assegnati alle teste di serie via via
     * meno alte man mano che i numeri di seed superano il numero di
     * qualificati.
     */
    public static int[] seedOrder(int size) {
        if (size < 1 || (size & (size - 1)) != 0) {
            throw new IllegalArgumentException("size deve essere una potenza di 2, ricevuto: " + size);
        }
        int[] positions = {1};
        while (positions.length < size) {
            int len = positions.length;
            int[] next = new int[len * 2];
            for (int i = 0; i < len; i++) {
                next[2 * i] = positions[i];
                next[2 * i + 1] = 2 * len + 1 - positions[i];
            }
            positions = next;
        }
        return positions;
    }
}
