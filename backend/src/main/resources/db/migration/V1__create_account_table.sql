CREATE TABLE account
(
    id            uuid         NOT NULL,
    email         varchar(320) NOT NULL,
    full_name     varchar(255) NOT NULL,
    password_hash varchar(255) NOT NULL,
    role          varchar(20)  NOT NULL,

    CONSTRAINT pk_account PRIMARY KEY (id),
    CONSTRAINT uq_account_email UNIQUE (email),
    CONSTRAINT ck_account_role CHECK (role IN ('ADMIN', 'MANAGER'))
);

COMMENT ON TABLE account IS 'Учётные записи пользователей сервиса';
COMMENT ON COLUMN account.id IS 'Уникальный идентификатор учётной записи в формате UUID версии 7';
COMMENT ON COLUMN account.email IS 'Адрес электронной почты пользователя';
COMMENT ON COLUMN account.full_name IS 'Фамилия, имя и отчество пользователя';
COMMENT ON COLUMN account.password_hash IS 'Хеш пароля пользователя';
COMMENT ON COLUMN account.role IS 'Роль пользователя: администратор или менеджер';
