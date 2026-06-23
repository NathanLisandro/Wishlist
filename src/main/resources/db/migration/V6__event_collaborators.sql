-- ============================================================
-- V6__event_collaborators.sql  ·  Co-gestão de evento
-- ------------------------------------------------------------
-- Permite que outra conta (ex.: o noivo, um padrinho) ajude a gerenciar o evento.
--   · event.manage_invite_token -> token do link de convite (null = sem convite ativo);
--     regenerar = novo token, invalida o link antigo.
--   · event_collaborator         -> contas que co-gerenciam o evento (além do dono).
-- O dono continua sendo event.account_id; co-gestores NÃO mexem em pagamento
-- (Mercado Pago é da conta do dono) nem veem outros eventos do dono.
-- ============================================================

ALTER TABLE event ADD COLUMN manage_invite_token text;

CREATE TABLE event_collaborator (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id    uuid NOT NULL REFERENCES event(id)   ON DELETE CASCADE,
    account_id  uuid NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    created_at  timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_event_collaborator UNIQUE (event_id, account_id)
);

CREATE INDEX idx_collaborator_account ON event_collaborator(account_id);
CREATE INDEX idx_collaborator_event   ON event_collaborator(event_id);
