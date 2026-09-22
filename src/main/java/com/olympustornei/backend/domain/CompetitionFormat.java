package com.olympustornei.backend.domain;

/**
 * Formato di competizione scelto dall'admin alla creazione della categoria:
 * {@code GIRONE} gioca prima il girone all'italiana e genera il tabellone
 * automaticamente al termine (vedi {@code MatchResultService}); {@code
 * TABELLONE} salta il girone e va direttamente alla fase a eliminazione
 * diretta, con accoppiamenti generati a caso o inseriti a mano dall'admin.
 */
public enum CompetitionFormat {
    GIRONE,
    TABELLONE
}
