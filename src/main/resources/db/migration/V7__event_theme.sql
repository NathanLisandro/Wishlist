-- ============================================================
-- V7__event_theme.sql  ·  Personalização visual do site do casal
-- PostgreSQL · ver docs/SDD.md
-- ------------------------------------------------------------
-- O casal escolhe a aparência da página pública:
--   · theme_palette      -> paleta pré-definida (whitelist no domínio: ROSE/AZUL/VERDE/...)
--   · accent_color       -> cor de destaque #RRGGBB (opcional; sobrepõe o accent da paleta)
--   · background_url      -> imagem de fundo do site (caminho público em /media/**, opcional)
--   · background_overlay  -> opacidade do overlay escuro sobre o fundo, 0..100 (legibilidade)
-- Tudo opcional; valores nulos = paleta padrão (ROSE), sem fundo. Paleta como text
-- (não enum Postgres) para adicionar novas paletas sem migration; o domínio valida.
-- ============================================================

ALTER TABLE event ADD COLUMN theme_palette      text;
ALTER TABLE event ADD COLUMN accent_color       text;
ALTER TABLE event ADD COLUMN background_url      text;
ALTER TABLE event ADD COLUMN background_overlay smallint;
