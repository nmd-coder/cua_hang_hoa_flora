CREATE TABLE checkout_quotes (
 id UUID PRIMARY KEY,
 user_id UUID NOT NULL,
 snapshot JSONB NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 expires_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX checkout_quotes_user_expiry ON checkout_quotes(user_id,expires_at);