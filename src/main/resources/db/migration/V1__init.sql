CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE tournaments (
    id     BIGSERIAL PRIMARY KEY,
    name   VARCHAR(200) NOT NULL,
    season VARCHAR(50),
    status VARCHAR(20)  NOT NULL CHECK (status IN ('DRAFT', 'ACTIVE', 'COMPLETED'))
);

CREATE TABLE categories (
    id                BIGSERIAL PRIMARY KEY,
    tournament_id     BIGINT       NOT NULL REFERENCES tournaments (id) ON DELETE CASCADE,
    name              VARCHAR(20)  NOT NULL CHECK (name IN ('GOLD', 'SILVER', 'BRONZE')),
    match_format      VARCHAR(20)  NOT NULL CHECK (match_format IN ('SINGLE', 'MULTI')),
    sub_matches_count INT          NOT NULL DEFAULT 1,
    phase             VARCHAR(20)  NOT NULL DEFAULT 'GIRONE' CHECK (phase IN ('GIRONE', 'TABELLONE', 'CONCLUSA')),
    schedule_locked   BOOLEAN      NOT NULL DEFAULT FALSE,
    UNIQUE (tournament_id, name)
);

CREATE TABLE teams (
    id          BIGSERIAL PRIMARY KEY,
    category_id BIGINT       NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    name        VARCHAR(200) NOT NULL
);

CREATE TABLE players (
    id      BIGSERIAL PRIMARY KEY,
    team_id BIGINT       NOT NULL REFERENCES teams (id) ON DELETE CASCADE,
    name    VARCHAR(200) NOT NULL
);

CREATE TABLE matchday_rounds (
    id           BIGSERIAL PRIMARY KEY,
    category_id  BIGINT NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    round_number INT    NOT NULL,
    UNIQUE (category_id, round_number)
);

CREATE TABLE matches (
    id                  BIGSERIAL PRIMARY KEY,
    category_id         BIGINT      NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    phase               VARCHAR(20) NOT NULL CHECK (phase IN ('GIRONE', 'TABELLONE')),
    round_id            BIGINT      REFERENCES matchday_rounds (id) ON DELETE CASCADE,
    bracket_round_index INT,
    bracket_slot        INT,
    home_team_id        BIGINT      NOT NULL REFERENCES teams (id),
    away_team_id        BIGINT      NOT NULL REFERENCES teams (id),
    status              VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'PLAYED')),
    result_type         VARCHAR(20) CHECK (result_type IN ('WIN_HOME', 'WIN_HOME_TB', 'WIN_AWAY', 'WIN_AWAY_TB')),
    winner_team_id      BIGINT      REFERENCES teams (id)
);

CREATE INDEX idx_matches_category ON matches (category_id);
CREATE INDEX idx_matches_round ON matches (round_id);

CREATE TABLE sub_matches (
    id              BIGSERIAL PRIMARY KEY,
    match_id        BIGINT NOT NULL REFERENCES matches (id) ON DELETE CASCADE,
    ordine          INT    NOT NULL,
    home_player1_id BIGINT REFERENCES players (id),
    home_player2_id BIGINT REFERENCES players (id),
    away_player1_id BIGINT REFERENCES players (id),
    away_player2_id BIGINT REFERENCES players (id),
    UNIQUE (match_id, ordine)
);

CREATE TABLE set_scores (
    id            BIGSERIAL PRIMARY KEY,
    sub_match_id  BIGINT NOT NULL REFERENCES sub_matches (id) ON DELETE CASCADE,
    set_number    INT    NOT NULL,
    home_games    INT    NOT NULL,
    away_games    INT    NOT NULL,
    UNIQUE (sub_match_id, set_number)
);
