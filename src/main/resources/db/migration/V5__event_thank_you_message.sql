-- ============================================================
-- V5__event_thank_you_message.sql  ·  Mensagem de agradecimento
-- ------------------------------------------------------------
-- Texto exibido ao convidado DEPOIS que ele confirma os presentes escolhidos.
-- Configurável pelo casal; opcional (há um padrão no front).
-- ============================================================

ALTER TABLE event ADD COLUMN thank_you_message text;
