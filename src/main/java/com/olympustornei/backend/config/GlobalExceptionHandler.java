package com.olympustornei.backend.config;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Traduce le eccezioni in un corpo JSON {"message": "..."} coerente,
 * indipendentemente dal messaggio dell'eccezione originale. Necessario perché
 * il comportamento di default di Spring Boot (proprietà
 * {@code server.error.include-message=always}) non espone il {@code reason}
 * di {@link ResponseStatusException} nel corpo di {@code /error} su questa
 * versione dello stack (verificato con test end-to-end: il campo "message"
 * resta assente anche con la proprietà impostata) — gestendo le eccezioni qui
 * evitiamo del tutto quel meccanismo.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Credenziali non valide"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        String message = ex.getReason() != null ? ex.getReason() : defaultMessageFor(ex.getStatusCode());
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", message));
    }

    private String defaultMessageFor(HttpStatusCode status) {
        if (status.equals(HttpStatus.NOT_FOUND)) {
            return "Risorsa non trovata";
        }
        if (status.equals(HttpStatus.UNAUTHORIZED)) {
            return "Non autorizzato";
        }
        return status.toString();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", message.isEmpty() ? "Dati non validi" : message));
    }
}
