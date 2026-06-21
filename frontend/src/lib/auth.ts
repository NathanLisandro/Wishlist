// Armazenamento de tokens no navegador (localStorage).
// Admin: token único. Convidado: token por slug de evento + dados da sessão.

import type { SessaoConvidado } from "./types";

const ADMIN_KEY = "lp.admin.token";
const ADMIN_NOME = "lp.admin.nome";
const guestKey = (slug: string) => `lp.guest.${slug}`;

function browser() {
  return typeof window !== "undefined";
}

export function setAdmin(token: string, nome: string) {
  if (!browser()) return;
  localStorage.setItem(ADMIN_KEY, token);
  localStorage.setItem(ADMIN_NOME, nome);
}

export function getAdminToken(): string | null {
  return browser() ? localStorage.getItem(ADMIN_KEY) : null;
}

export function getAdminNome(): string | null {
  return browser() ? localStorage.getItem(ADMIN_NOME) : null;
}

export function clearAdmin() {
  if (!browser()) return;
  localStorage.removeItem(ADMIN_KEY);
  localStorage.removeItem(ADMIN_NOME);
}

export function setGuest(slug: string, sessao: SessaoConvidado) {
  if (!browser()) return;
  localStorage.setItem(guestKey(slug), JSON.stringify(sessao));
}

export function getGuest(slug: string): SessaoConvidado | null {
  if (!browser()) return null;
  const raw = localStorage.getItem(guestKey(slug));
  return raw ? (JSON.parse(raw) as SessaoConvidado) : null;
}

export function clearGuest(slug: string) {
  if (!browser()) return;
  localStorage.removeItem(guestKey(slug));
}
