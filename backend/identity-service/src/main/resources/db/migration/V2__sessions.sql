CREATE TABLE sessions (
    token_hash CHAR(64) PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);
CREATE INDEX sessions_user_id_idx ON sessions(user_id);