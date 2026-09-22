-- Supporto al tabellone (fase TABELLONE): i match dei round successivi al
-- primo possono avere una o entrambe le squadre non ancora note (in attesa
-- del risultato del match che alimenta quello slot), quindi le colonne
-- squadra devono poter essere NULL. In fase GIRONE continuano ad essere
-- sempre valorizzate a creazione, quindi il comportamento esistente non
-- cambia.
ALTER TABLE matches ALTER COLUMN home_team_id DROP NOT NULL;
ALTER TABLE matches ALTER COLUMN away_team_id DROP NOT NULL;

-- Numero di round del tabellone generato per la categoria (log2 della
-- dimensione del tabellone, arrotondata alla potenza di 2 superiore al
-- numero di squadre qualificate). Serve per riconoscere quando un risultato
-- riguarda la finale (nessun round successivo in cui avanzare il vincitore).
ALTER TABLE categories ADD COLUMN bracket_total_rounds INT;
