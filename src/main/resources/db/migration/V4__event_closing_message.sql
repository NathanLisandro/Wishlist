-- ============================================================
-- V4__event_closing_message.sql  ·  Frase de despedida da landing
-- ------------------------------------------------------------
-- Mensagem final exibida no rodapé da página pública (ex.: "Mal podemos
-- esperar por você."). Configurável pelo casal; opcional (há um padrão no front).
-- ============================================================

ALTER TABLE event ADD COLUMN closing_message text;
