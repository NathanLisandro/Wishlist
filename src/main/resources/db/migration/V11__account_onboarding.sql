-- ============================================================
-- V11__account_onboarding.sql  ·  Tour de boas-vindas (onboarding)
-- PostgreSQL · ver docs/SDD.md
-- ------------------------------------------------------------
-- onboarding_completed -> controla o passo a passo guiado exibido apenas para
--   CONTAS NOVAS. Contas que já existem antes deste recurso não são "novas":
--   marcamos como concluído para não exibir o tour a quem já conhece a plataforma.
--   Contas criadas a partir daqui nascem com false (default) e veem o tour.
-- ============================================================

ALTER TABLE account ADD COLUMN onboarding_completed boolean NOT NULL DEFAULT false;

-- Backfill: toda conta pré-existente é considerada já integrada.
UPDATE account SET onboarding_completed = true;
