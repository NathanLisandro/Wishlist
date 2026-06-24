-- ============================================================
-- V9__gift_price.sql  ·  Preço estimado do presente
-- PostgreSQL · ver docs/SDD.md
-- ------------------------------------------------------------
-- estimated_price -> preço de mercado estimado do item (BRL), preenchido pela IA
--   (ver fase de importação por IA). Opcional. Usado SÓ pelo dono para o "ticket
--   médio" da lista; nunca exposto na página pública.
-- ============================================================

ALTER TABLE gift_item ADD COLUMN estimated_price numeric(12,2);
