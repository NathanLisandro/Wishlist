-- ============================================================
-- V1__init.sql  ·  Schema inicial — Plataforma de Lista de Presentes
-- PostgreSQL · 3ª Forma Normal · ver docs/SDD.md (§4)
-- Local no repo: src/main/resources/db/migration/V1__init.sql
-- ============================================================

-- Extensões
CREATE EXTENSION IF NOT EXISTS pgcrypto;  -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS citext;    -- e-mail case-insensitive

-- ------------------------------------------------------------
-- Enums de status (categoria do evento é tabela de domínio, ver event_category)
-- ------------------------------------------------------------
CREATE TYPE account_status    AS ENUM ('ACTIVE', 'SUSPENDED');
CREATE TYPE event_status      AS ENUM ('DRAFT', 'PUBLISHED', 'CLOSED');
CREATE TYPE gift_status       AS ENUM ('AVAILABLE', 'SOLD_OUT', 'HIDDEN');
CREATE TYPE claim_status      AS ENUM ('CLAIMED', 'CANCELLED');
CREATE TYPE rsvp_status       AS ENUM ('CONFIRMED', 'DECLINED', 'CANCELLED');
CREATE TYPE money_gift_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'REFUNDED');
CREATE TYPE import_type       AS ENUM ('GIFTS', 'GUESTS');
CREATE TYPE import_status     AS ENUM ('PROCESSING', 'DONE', 'FAILED');
CREATE TYPE actor_type        AS ENUM ('ADMIN', 'GUEST', 'SYSTEM');

-- ------------------------------------------------------------
-- Trigger util: atualiza updated_at automaticamente
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- ACCOUNT (tenant = casal)
-- ============================================================
CREATE TABLE account (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    email          citext  NOT NULL UNIQUE,
    password_hash  text    NOT NULL,
    display_name   text    NOT NULL,
    email_verified boolean NOT NULL DEFAULT false,
    status         account_status NOT NULL DEFAULT 'ACTIVE',
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_account_updated BEFORE UPDATE ON account
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- MERCADO_PAGO_CONFIG (1:1 com account; credenciais criptografadas em repouso)
-- ============================================================
CREATE TABLE mercado_pago_config (
    id                       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id               uuid  NOT NULL UNIQUE REFERENCES account(id) ON DELETE CASCADE,
    access_token_encrypted   bytea NOT NULL,
    public_key               text,
    webhook_secret_encrypted bytea,
    updated_at               timestamptz NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_mp_config_updated BEFORE UPDATE ON mercado_pago_config
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- EVENT_CATEGORY (tabela de domínio — extensível sem migration de schema)
-- ============================================================
CREATE TABLE event_category (
    id     smallint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code   text    NOT NULL UNIQUE,
    label  text    NOT NULL,
    active boolean NOT NULL DEFAULT true
);

INSERT INTO event_category (code, label) VALUES
    ('CASAMENTO',     'Casamento'),
    ('NOIVADO',       'Noivado'),
    ('CHA_PANELA',    'Chá de Panela'),
    ('CHA_COZINHA',   'Chá de Cozinha'),
    ('CHA_BAR',       'Chá Bar'),
    ('CHA_CASA_NOVA', 'Chá de Casa Nova'),
    ('CHA_LINGERIE',  'Chá de Lingerie'),
    ('BODAS',         'Bodas'),
    ('OUTRO',         'Outro');

-- ============================================================
-- EVENT
-- ============================================================
CREATE TABLE event (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id         uuid     NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    category_id        smallint NOT NULL REFERENCES event_category(id),
    slug               text     NOT NULL,
    name               text     NOT NULL,
    description        text,
    event_date         date,
    rsvp_enabled       boolean  NOT NULL DEFAULT true,
    rsvp_deadline      timestamptz,
    money_gift_enabled boolean  NOT NULL DEFAULT true,
    status             event_status NOT NULL DEFAULT 'DRAFT',
    created_at         timestamptz NOT NULL DEFAULT now(),
    updated_at         timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_event_account_slug UNIQUE (account_id, slug)
);
CREATE INDEX idx_event_account ON event(account_id);
CREATE TRIGGER trg_event_updated BEFORE UPDATE ON event
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- GUEST (PII criptografada; lookup por HMAC do telefone — nunca em claro)
-- ============================================================
CREATE TABLE guest (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id            uuid  NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    full_name_encrypted bytea NOT NULL,
    phone_encrypted     bytea NOT NULL,
    phone_hash          bytea NOT NULL,                 -- HMAC-SHA256(telefone)
    companions_allowed  smallint NOT NULL DEFAULT 0 CHECK (companions_allowed >= 0),
    created_at          timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_guest_event_phone UNIQUE (event_id, phone_hash)
);
CREATE INDEX idx_guest_event ON guest(event_id);

-- ============================================================
-- GIFT_ITEM
--   variant = "cor / material / acabamento" (inox, preto, cerâmica...)
-- ============================================================
CREATE TABLE gift_item (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id       uuid    NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    name           text    NOT NULL,
    variant        text,
    total_quantity integer NOT NULL CHECK (total_quantity > 0),
    product_link   text,
    image_url      text,
    status         gift_status NOT NULL DEFAULT 'AVAILABLE',
    natural_key    text    NOT NULL,                    -- hash p/ idempotência de import
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_gift_event_natural UNIQUE (event_id, natural_key)
);
CREATE INDEX idx_gift_event_status ON gift_item(event_id, status);
CREATE TRIGGER trg_gift_updated BEFORE UPDATE ON gift_item
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- GIFT_CLAIM (reserva idempotente; 1 unidade por linha)
--   · quantidade reservada NÃO é coluna (derivada de COUNT → 3NF)
--   · unique parcial impede o mesmo convidado reivindicar 2x o mesmo item
-- ============================================================
CREATE TABLE gift_claim (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    gift_item_id    uuid NOT NULL REFERENCES gift_item(id) ON DELETE CASCADE,
    guest_id        uuid NOT NULL REFERENCES guest(id)     ON DELETE CASCADE,
    status          claim_status NOT NULL DEFAULT 'CLAIMED',
    idempotency_key text NOT NULL UNIQUE,
    created_at      timestamptz NOT NULL DEFAULT now(),
    cancelled_at    timestamptz
);
CREATE UNIQUE INDEX uq_gift_claim_active
    ON gift_claim (gift_item_id, guest_id) WHERE status = 'CLAIMED';
CREATE INDEX idx_gift_claim_item  ON gift_claim(gift_item_id);
CREATE INDEX idx_gift_claim_guest ON gift_claim(guest_id);

-- ============================================================
-- RSVP (1 por convidado; nº de acompanhantes derivado de rsvp_companion → 3NF)
-- ============================================================
CREATE TABLE rsvp (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id     uuid NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    guest_id     uuid NOT NULL REFERENCES guest(id) ON DELETE CASCADE,
    status       rsvp_status NOT NULL,
    responded_at timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_rsvp_event_guest UNIQUE (event_id, guest_id)
);
CREATE INDEX idx_rsvp_event ON rsvp(event_id);
CREATE TRIGGER trg_rsvp_updated BEFORE UPDATE ON rsvp
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- RSVP_COMPANION (acompanhantes confirmados)
--   Limite (<= guest.companions_allowed) validado no caso de uso ConfirmarPresenca
-- ============================================================
CREATE TABLE rsvp_companion (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    rsvp_id    uuid NOT NULL REFERENCES rsvp(id) ON DELETE CASCADE,
    full_name  text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_rsvp_companion_rsvp ON rsvp_companion(rsvp_id);

-- ============================================================
-- MONEY_GIFT (presente em dinheiro via Mercado Pago)
-- ============================================================
CREATE TABLE money_gift (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id         uuid NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    guest_id         uuid REFERENCES guest(id) ON DELETE SET NULL,
    amount           numeric(12,2) NOT NULL CHECK (amount > 0),
    currency         char(3) NOT NULL DEFAULT 'BRL',
    message          text,
    mp_preference_id text,
    mp_payment_id    text UNIQUE,                       -- idempotência do webhook
    status           money_gift_status NOT NULL DEFAULT 'PENDING',
    created_at       timestamptz NOT NULL DEFAULT now(),
    updated_at       timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_money_gift_event  ON money_gift(event_id);
CREATE INDEX idx_money_gift_status ON money_gift(status);
CREATE TRIGGER trg_money_gift_updated BEFORE UPDATE ON money_gift
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- PAYMENT_WEBHOOK_EVENT (idempotência/auditoria das notificações do MP)
-- ============================================================
CREATE TABLE payment_webhook_event (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    money_gift_id uuid REFERENCES money_gift(id) ON DELETE SET NULL,
    mp_event_id   text  NOT NULL UNIQUE,                -- dedup de notificações repetidas
    payload       jsonb NOT NULL,
    processed_at  timestamptz
);

-- ============================================================
-- IMPORT_JOB (rastreamento de importações CSV/XLSX)
-- ============================================================
CREATE TABLE import_job (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id     uuid NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    type         import_type   NOT NULL,
    filename     text          NOT NULL,
    status       import_status NOT NULL DEFAULT 'PROCESSING',
    rows_total   integer NOT NULL DEFAULT 0,
    rows_ok      integer NOT NULL DEFAULT 0,
    rows_failed  integer NOT NULL DEFAULT 0,
    error_report jsonb,
    created_at   timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_import_job_event ON import_job(event_id);

-- ============================================================
-- AUDIT_LOG (auditoria de segurança)
-- ============================================================
CREATE TABLE audit_log (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id uuid REFERENCES account(id) ON DELETE SET NULL,
    actor_type actor_type NOT NULL,
    actor_id   uuid,
    action     text NOT NULL,
    entity     text,
    entity_id  text,
    metadata   jsonb,
    ip         inet,
    user_agent text,
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_account_created ON audit_log(account_id, created_at);
