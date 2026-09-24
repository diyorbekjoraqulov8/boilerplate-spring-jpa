ALTER TABLE sessions ADD COLUMN refresh_token_hash          VARCHAR(64);
ALTER TABLE sessions ADD COLUMN previous_refresh_token_hash VARCHAR(64);

CREATE UNIQUE INDEX uk_sessions_refresh_hash
    ON sessions (refresh_token_hash) WHERE refresh_token_hash IS NOT NULL;

CREATE INDEX idx_sessions_prev_refresh_hash
    ON sessions (previous_refresh_token_hash) WHERE previous_refresh_token_hash IS NOT NULL;