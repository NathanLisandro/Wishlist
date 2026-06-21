-- ============================================================
-- V3__event_photo_and_details.sql  ·  Foto e detalhes do evento
-- PostgreSQL · ver docs/SDD.md
-- ------------------------------------------------------------
-- Personalização da página pública do evento pelo casal:
--   · photo_url       -> caminho público da foto do casal (servida em /media/**)
--   · location        -> "Onde" (ex.: "Sítio Recanto Verde · Maringá")
--   · couple_message  -> recado/citação exibido na landing (a "quote")
-- Tudo opcional. A foto é um caminho relativo (ex.: /media/eventos/<id>/<arquivo>),
-- o binário fica no storage configurado (local em dev, S3 em prod no futuro).
-- ============================================================

ALTER TABLE event ADD COLUMN photo_url      text;
ALTER TABLE event ADD COLUMN location       text;
ALTER TABLE event ADD COLUMN couple_message text;
