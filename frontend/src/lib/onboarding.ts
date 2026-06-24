// Estado do tour de boas-vindas (onboarding) — exibido apenas para CONTAS NOVAS.
//
// Fonte de verdade: backend (`conta.onboardingConcluido`). O localStorage é só um
// cache para evitar buscar `/account/me` em todo acesso de quem já está integrado,
// e para não repetir um mesmo tour já visto neste navegador.

import { api } from "./api";
import type { MeResponse } from "./types";

const DONE = "lp.onboarding.done"; // onboarding 100% concluído (espelha o backend)
const tourKey = (nome: string) => `lp.tour.${nome}`; // tour visto neste navegador

function browser() {
  return typeof window !== "undefined";
}

/** Onboarding já concluído (cache local) — evita chamar a API. */
export function onboardingConcluidoLocal(): boolean {
  return browser() && localStorage.getItem(DONE) === "1";
}

function marcarConcluidoLocal() {
  if (browser()) localStorage.setItem(DONE, "1");
}

/** Um tour específico já foi exibido neste navegador? */
export function tourVisto(nome: string): boolean {
  return browser() && localStorage.getItem(tourKey(nome)) === "1";
}

/** Marca um tour como exibido para não repetir neste navegador. */
export function marcarTourVisto(nome: string) {
  if (browser()) localStorage.setItem(tourKey(nome), "1");
}

/**
 * Limpa o cache local de onboarding. Deve ser chamado no login: o cache é por
 * navegador, não por conta — sem isso, trocar de conta no mesmo navegador faria
 * uma conta nova herdar o "já concluído" da anterior. O backend é a fonte da
 * verdade; ao limpar, a próxima sessão reavalia via /account/me.
 */
export function resetarOnboardingLocal() {
  if (!browser()) return;
  localStorage.removeItem(DONE);
  localStorage.removeItem(tourKey("dashboard"));
  localStorage.removeItem(tourKey("gerenciar"));
}

/**
 * Busca a conta autenticada; sincroniza o cache local quando já integrada.
 * Nunca lança: qualquer falha (rede/sessão) retorna null e o tour não é exibido
 * — o tratamento de sessão expirada fica a cargo das chamadas de dados da tela.
 */
export async function buscarMe(token: string): Promise<MeResponse | null> {
  try {
    const me = await api.get<MeResponse>("/account/me", token);
    if (me.onboardingConcluido) marcarConcluidoLocal();
    return me;
  } catch {
    return null;
  }
}

/** Marca o onboarding como concluído no backend (idempotente) e no cache. */
export async function concluirOnboarding(token: string) {
  marcarConcluidoLocal();
  try {
    await api.post("/account/onboarding/concluir", undefined, token);
  } catch {
    // Não bloqueia a UX; o cache local já evita reexibição neste navegador.
  }
}
