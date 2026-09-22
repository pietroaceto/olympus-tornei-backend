package com.olympustornei.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Accoppiamenti del primo turno decisi a mano dall'admin per una categoria
 * "Tabellone diretto": {@code slots} ha lunghezza pari alla potenza di 2
 * successiva al numero di squadre, ogni elemento è l'id della squadra in
 * quello slot oppure {@code null} per un BYE. Gli slot adiacenti (0-1, 2-3,
 * ...) giocano l'uno contro l'altro al primo turno.
 */
public record ManualBracketRequest(
        @NotEmpty List<Long> slots
) {
}
