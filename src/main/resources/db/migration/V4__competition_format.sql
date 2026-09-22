ALTER TABLE categories
    ADD COLUMN competition_format VARCHAR(20) NOT NULL DEFAULT 'GIRONE'
        CHECK (competition_format IN ('GIRONE', 'TABELLONE'));
