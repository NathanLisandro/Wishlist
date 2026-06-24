-- ============================================================
-- V8__event_animation.sql  ·  Estilo de animação do site
-- PostgreSQL · ver docs/SDD.md
-- ------------------------------------------------------------
-- animation_style -> estilo de animação de entrada do site público
--   (whitelist no domínio: NENHUMA/SUAVE/CASCATA/FLUTUAR). Nulo = SUAVE (atual).
--   text (não enum Postgres) para adicionar estilos sem migration; o CSS vive no front.
-- ============================================================

ALTER TABLE event ADD COLUMN animation_style text;
