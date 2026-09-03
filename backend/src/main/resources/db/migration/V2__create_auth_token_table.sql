CREATE TABLE auth_token
(
    id         uuid        NOT NULL,
    account_id uuid        NOT NULL,
    token_hash varchar(64) NOT NULL,
    created_at timestamptz NOT NULL,
    expires_at timestamptz NOT NULL,
    revoked_at timestamptz,

    CONSTRAINT pk_auth_token PRIMARY KEY (id),
    CONSTRAINT uq_auth_token_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_auth_token_account FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE,
    CONSTRAINT ck_auth_token_expiration CHECK (expires_at > created_at)
);

CREATE INDEX ix_auth_token_account_id ON auth_token (account_id);

COMMENT ON TABLE auth_token IS 'Токены авторизации пользователей';
COMMENT ON COLUMN auth_token.id IS 'Уникальный идентификатор токена в формате UUID версии 7';
COMMENT ON COLUMN auth_token.account_id IS 'Идентификатор учётной записи пользователя';
COMMENT ON COLUMN auth_token.token_hash IS 'SHA-256 хеш Bearer Token';
COMMENT ON COLUMN auth_token.created_at IS 'Дата и время создания токена';
COMMENT ON COLUMN auth_token.expires_at IS 'Дата и время окончания действия токена';
COMMENT ON COLUMN auth_token.revoked_at IS 'Дата и время отзыва токена';
