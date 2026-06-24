-- ============================================================
-- V10__event_surprise_mode.sql  ·  Modo surpresa
-- PostgreSQL · ver docs/SDD.md
-- ------------------------------------------------------------
-- surprise_mode -> quando ligado, só o co-gestor (terceiro) mexe nos presentes;
--   o dono não vê nem edita a lista (para que o casal seja surpreendido). Construído
--   sobre a co-gestão existente (event_collaborator). Default false.
-- ============================================================

ALTER TABLE event ADD COLUMN surprise_mode boolean NOT NULL DEFAULT false;
