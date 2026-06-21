-- ============================================================
-- V2__guest_access_log.sql  ·  Rastreamento de acessos do convidado
-- PostgreSQL · ver docs/SDD.md (§7) e CLAUDE.md (regra de PII)
-- ------------------------------------------------------------
-- Objetivo: registrar QUEM acessou o sistema (convidado), QUANDO e O QUE fez,
-- para o painel do admin acompanhar acessos.
--
-- PRIVACIDADE (regra inegociável do projeto): NÃO armazenamos o telefone em
-- claro aqui. A identificação é feita por:
--   · guest_id   -> liga ao convidado (nome/telefone ficam só em `guest`, cifrados)
--   · phone_hash -> HMAC-SHA256 do telefone (mesmo esquema de `guest.phone_hash`),
--                   permite rastrear tentativas mesmo sem guest_id (ex.: telefone
--                   não cadastrado), sem nunca expor o número.
-- O telefone real só é decifrado no painel do admin, sob autorização.
-- ============================================================

CREATE TYPE guest_access_action AS ENUM (
    'LOGIN_SUCCESS',   -- telefone bateu com a lista; sessão criada
    'LOGIN_FAILED',    -- telefone não encontrado no evento
    'LIST_GIFTS',      -- listou presentes
    'CLAIM_GIFT',      -- reservou presente
    'CANCEL_CLAIM',    -- cancelou reserva
    'MONEY_GIFT',      -- iniciou presente em dinheiro
    'RSVP_CONFIRM',    -- confirmou presença
    'RSVP_CANCEL'      -- cancelou presença
);

CREATE TABLE guest_access_log (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id    uuid NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    guest_id    uuid REFERENCES guest(id) ON DELETE SET NULL,   -- NULL em LOGIN_FAILED
    phone_hash  bytea,                                          -- HMAC-SHA256; nunca telefone em claro
    action      guest_access_action NOT NULL,
    ip          text,                                           -- text (não inet) p/ mapeamento JPA simples
    user_agent  text,
    metadata    jsonb,                                          -- contexto extra (ex.: gift_item_id)
    created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_guest_access_event_created ON guest_access_log(event_id, created_at);
CREATE INDEX idx_guest_access_phone_hash    ON guest_access_log(phone_hash);
CREATE INDEX idx_guest_access_guest         ON guest_access_log(guest_id);
