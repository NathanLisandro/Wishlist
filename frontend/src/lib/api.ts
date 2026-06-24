// Wrapper de fetch para a API. Anexa Bearer token e traduz problem+json em erro amigável.

// Base no NAVEGADOR: vazio = caminho relativo (mesma origem) que o Next faz proxy
// para o backend (ver next.config.ts). Funciona igual em localhost e via ngrok.
const PUBLIC_API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";
// Base no SERVIDOR (SSR/Route handlers): fala direto com o backend interno.
const INTERNAL_API_BASE = process.env.API_INTERNAL_URL ?? "http://localhost:8080";

/** Base usada para fetch — relativa no browser, absoluta interna no servidor. */
function apiBase(): string {
  return typeof window === "undefined" ? INTERNAL_API_BASE : PUBLIC_API_BASE;
}

/** Compat: usada por uploads/downloads no client (relativa → proxy do Next). */
export const API_BASE = PUBLIC_API_BASE;

/**
 * Caminho de uma foto para usar em <img src>. O backend devolve caminhos relativos
 * (ex.: /media/eventos/<id>/<arquivo>); deixamos relativo para o navegador resolver
 * contra a origem atual (e o Next faz proxy para o backend). URLs absolutas passam direto.
 */
export function mediaUrl(caminho: string | null | undefined): string | null {
  if (!caminho) return null;
  if (/^https?:\/\//i.test(caminho)) return caminho;
  return caminho.startsWith("/") ? caminho : `/${caminho}`;
}

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

/** Sessão inválida/expirada ou apontando para recurso que não existe mais (token velho). */
export function ehSessaoInvalida(err: unknown): boolean {
  // 401: token inválido. 403: token expirou e a requisição seguiu anônima numa rota
  // protegida (Spring devolve 403, não 401). 404: token aponta p/ recurso que sumiu.
  return err instanceof ApiError && (err.status === 401 || err.status === 403 || err.status === 404);
}

type Opcoes = {
  method?: string;
  body?: unknown;
  token?: string | null;
  headers?: Record<string, string>;
  /** Para uploads multipart: passa FormData direto em body sem serializar. */
  form?: FormData;
};

async function request<T>(caminho: string, opcoes: Opcoes = {}): Promise<T> {
  const headers: Record<string, string> = { ...(opcoes.headers ?? {}) };
  if (opcoes.token) headers["Authorization"] = `Bearer ${opcoes.token}`;

  let body: BodyInit | undefined;
  if (opcoes.form) {
    body = opcoes.form;
  } else if (opcoes.body !== undefined) {
    headers["Content-Type"] = "application/json";
    body = JSON.stringify(opcoes.body);
  }

  const resp = await fetch(`${apiBase()}${caminho}`, {
    method: opcoes.method ?? (body ? "POST" : "GET"),
    headers,
    body,
    cache: "no-store",
  });

  if (resp.status === 204) return undefined as T;

  const texto = await resp.text();
  const dados = texto ? safeJson(texto) : null;

  if (!resp.ok) {
    const detalhe =
      (dados && (dados.detail || dados.message)) || `Erro ${resp.status}`;
    throw new ApiError(resp.status, detalhe);
  }
  return dados as T;
}

function safeJson(texto: string): any {
  try {
    return JSON.parse(texto);
  } catch {
    return { detail: texto };
  }
}

export const api = {
  get: <T>(caminho: string, token?: string | null) =>
    request<T>(caminho, { method: "GET", token }),
  post: <T>(caminho: string, body?: unknown, token?: string | null, headers?: Record<string, string>) =>
    request<T>(caminho, { method: "POST", body, token, headers }),
  put: <T>(caminho: string, body?: unknown, token?: string | null) =>
    request<T>(caminho, { method: "PUT", body, token }),
  del: <T>(caminho: string, token?: string | null) =>
    request<T>(caminho, { method: "DELETE", token }),
  upload: <T>(caminho: string, form: FormData, token?: string | null) =>
    request<T>(caminho, { method: "POST", form, token }),
};
