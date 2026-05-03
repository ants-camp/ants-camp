-- =========================
-- 1. Schema 생성
-- =========================
CREATE SCHEMA IF NOT EXISTS "user";
CREATE SCHEMA IF NOT EXISTS trade;
CREATE SCHEMA IF NOT EXISTS asset;
CREATE SCHEMA IF NOT EXISTS competition;
CREATE SCHEMA IF NOT EXISTS ranking;
CREATE SCHEMA IF NOT EXISTS assistant;
CREATE SCHEMA IF NOT EXISTS notification;

-- =========================
-- 2. 사용자 생성
-- =========================
DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'antcamp') THEN
        CREATE ROLE antcamp LOGIN PASSWORD 'p@ssw0rd';
    END IF;
END
$$;

-- =========================
-- 3. Schema 권한 부여
-- =========================
GRANT USAGE, CREATE ON SCHEMA "user" TO antcamp;
GRANT USAGE, CREATE ON SCHEMA trade TO antcamp;
GRANT USAGE, CREATE ON SCHEMA asset TO antcamp;
GRANT USAGE, CREATE ON SCHEMA competition TO antcamp;
GRANT USAGE, CREATE ON SCHEMA ranking TO antcamp;
GRANT USAGE, CREATE ON SCHEMA assistant TO antcamp;
GRANT USAGE, CREATE ON SCHEMA notification TO antcamp;

-- =========================
-- 4. 테이블 생성
-- =========================

-- competition schema
CREATE TABLE IF NOT EXISTS competition.p_competitions (
    competition_id      UUID            NOT NULL,
    name                VARCHAR(100)    NOT NULL,
    type                VARCHAR(50)     NOT NULL,
    status              VARCHAR(50)     NOT NULL,
    description         TEXT            NOT NULL,
    first_seed          INT             NOT NULL,
    is_readable         BOOLEAN         NOT NULL DEFAULT FALSE,
    register_start_at   TIMESTAMP       NOT NULL,
    register_end_at     TIMESTAMP       NOT NULL,
    competition_start_at TIMESTAMP      NOT NULL,
    competition_end_at  TIMESTAMP       NOT NULL,
    min_participants    INT             NOT NULL,
    max_participants    INT             NOT NULL,
    current_registers   INT             NOT NULL DEFAULT 0,
    created_at          TIMESTAMP       NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    updated_at          TIMESTAMP,
    updated_by          VARCHAR(255),
    deleted_at          TIMESTAMP,
    deleted_by          VARCHAR(255),
    PRIMARY KEY (competition_id)
);

CREATE TABLE IF NOT EXISTS competition.p_competition_participant (
    participant_id  UUID            NOT NULL,
    user_id         UUID            NOT NULL,
    nickname        VARCHAR(100)    NOT NULL,
    competition_id  UUID            NOT NULL,
    created_at      TIMESTAMP       NOT NULL,
    created_by      VARCHAR(255)    NOT NULL,
    updated_at      TIMESTAMP,
    updated_by      VARCHAR(255),
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(255),
    PRIMARY KEY (participant_id),
    CONSTRAINT uq_competition_participant_user_competition UNIQUE (user_id, competition_id)
);

CREATE TABLE IF NOT EXISTS competition.p_competition_change_notices (
    notice_id           UUID            NOT NULL,
    competition_id      UUID            NOT NULL,
    competition_status  VARCHAR(50)     NOT NULL,
    before_contents     TEXT            NOT NULL,
    after_contents      TEXT            NOT NULL,
    reason              VARCHAR(255)    NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    updated_at          TIMESTAMP,
    updated_by          VARCHAR(255),
    deleted_at          TIMESTAMP,
    deleted_by          VARCHAR(255),
    PRIMARY KEY (notice_id)
);

-- user schema
CREATE TABLE IF NOT EXISTS "user".p_user (
    user_id     UUID            NOT NULL,
    email       VARCHAR(255)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    role        VARCHAR(50)     NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'PLAYER')),
    phone       VARCHAR(255)    NOT NULL,
    status      VARCHAR(50)     NOT NULL CHECK (status IN ('ACTIVE', 'BLOCKED', 'WITHDRAWN', 'DELETED')),
    created_at  TIMESTAMP       NOT NULL,
    created_by  VARCHAR(255)    NOT NULL,
    updated_at  TIMESTAMP,
    updated_by  VARCHAR(255),
    deleted_at  TIMESTAMP,
    deleted_by  VARCHAR(255),
    PRIMARY KEY (user_id),
    CONSTRAINT uq_user_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS "user".p_refresh_token (
    id          UUID                        NOT NULL,
    user_id     UUID                        NOT NULL,
    token       VARCHAR(500)                NOT NULL,
    expires_at  TIMESTAMP WITH TIME ZONE    NOT NULL,
    PRIMARY KEY (id)
);

-- asset schema
CREATE TABLE IF NOT EXISTS asset.p_accounts (
    account_id      UUID            NOT NULL,
    user_id         UUID            NOT NULL,
    account_number  VARCHAR(255)    NOT NULL,
    type            VARCHAR(50)     NOT NULL,
    account_amount  BIGINT          NOT NULL,
    PRIMARY KEY (account_id),
    CONSTRAINT uq_accounts_account_number UNIQUE (account_number)
);

CREATE TABLE IF NOT EXISTS asset.p_holdings (
    holding_id      UUID            NOT NULL,
    account_id      UUID            NOT NULL,
    stock_code      VARCHAR(20)     NOT NULL,
    stock_amount    INT             NOT NULL,
    buy_price       BIGINT          NOT NULL,
    final_price     BIGINT          NOT NULL,
    PRIMARY KEY (holding_id),
    CONSTRAINT uk_holdings_account_stock UNIQUE (account_id, stock_code)
);

-- trade schema
CREATE TABLE IF NOT EXISTS trade.p_trade (
    trade_id        UUID            NOT NULL,
    account_id      UUID            NOT NULL,
    trade_type      VARCHAR(50),
    trade_at        TIMESTAMP,
    stock_code      VARCHAR(255)    NOT NULL,
    stock_amount    INT             NOT NULL,
    trade_status    VARCHAR(50),
    total_price     DOUBLE PRECISION NOT NULL,
    created_at      TIMESTAMP       NOT NULL,
    created_by      VARCHAR(255)    NOT NULL,
    updated_at      TIMESTAMP,
    updated_by      VARCHAR(255),
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(255),
    PRIMARY KEY (trade_id)
);

-- ranking schema
CREATE TABLE IF NOT EXISTS ranking.p_rankings (
    ranking_id      UUID            NOT NULL,
    competition_id  UUID            NOT NULL,
    user_id         UUID            NOT NULL,
    rank            VARCHAR(50),
    last_updated_at TIMESTAMP,
    is_finalized    BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL,
    created_by      VARCHAR(255)    NOT NULL,
    updated_at      TIMESTAMP,
    updated_by      VARCHAR(255),
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(255),
    PRIMARY KEY (ranking_id),
    CONSTRAINT uq_rankings_competition_user UNIQUE (competition_id, user_id)
);

-- notification schema
CREATE TABLE IF NOT EXISTS notification.p_slack_messages (
    slack_message_id    UUID            NOT NULL,
    slack_channel_id    VARCHAR(100)    NOT NULL,
    job                 VARCHAR(100)    NOT NULL,
    source              VARCHAR(20)     NOT NULL,
    deduplication_key   VARCHAR(255)    NOT NULL,
    severity            VARCHAR(20)     NOT NULL,
    title               VARCHAR(255)    NOT NULL,
    content             TEXT            NOT NULL,
    payload             JSONB           NOT NULL,
    ai_analysis         TEXT,
    status              VARCHAR(20)     NOT NULL,
    slack_message_ts    VARCHAR(50),
    resolution_action   VARCHAR(50),
    action_user_email   VARCHAR(100),
    created_at          TIMESTAMP       NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    updated_at          TIMESTAMP,
    updated_by          VARCHAR(255),
    deleted_at          TIMESTAMP,
    deleted_by          VARCHAR(255),
    PRIMARY KEY (slack_message_id)
);

-- =========================
-- 5. 기본 권한 설정 (테이블 자동 권한)
-- =========================
ALTER DEFAULT PRIVILEGES IN SCHEMA "user"
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO antcamp;
ALTER DEFAULT PRIVILEGES IN SCHEMA trade
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO antcamp;
ALTER DEFAULT PRIVILEGES IN SCHEMA asset
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO antcamp;
ALTER DEFAULT PRIVILEGES IN SCHEMA competition
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO antcamp;
ALTER DEFAULT PRIVILEGES IN SCHEMA ranking
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO antcamp;
ALTER DEFAULT PRIVILEGES IN SCHEMA assistant
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO antcamp;
ALTER DEFAULT PRIVILEGES IN SCHEMA notification
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO antcamp;
