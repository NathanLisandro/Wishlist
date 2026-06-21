/**
 * Formata um telefone brasileiro (DDD + número) enquanto a pessoa digita.
 * O código do país (+55) fica fixo fora do campo — aqui é só DDD + número.
 *   "67998437332"  -> "(67) 99843-7332"
 *   "6799843"      -> "(67) 99843"
 */
export function formatarTelefoneBR(valor: string): string {
  const d = valor.replace(/\D/g, "").slice(0, 11);
  if (d.length === 0) return "";
  if (d.length <= 2) return `(${d}`;
  const ddd = d.slice(0, 2);
  const resto = d.slice(2);
  if (resto.length <= 4) return `(${ddd}) ${resto}`;
  const corte = resto.length <= 8 ? resto.length - 4 : 5; // fixo→4 dígitos, celular→5+4
  return `(${ddd}) ${resto.slice(0, corte)}-${resto.slice(corte)}`;
}

/** Só os dígitos (para enviar à API junto do +55). */
export function apenasDigitos(valor: string): string {
  return valor.replace(/\D/g, "");
}

/**
 * Formata para exibição um telefone vindo da API (E.164, ex.: "+5567998437332").
 * Mostra como "(67) 99843-7332" quando é brasileiro; senão devolve como veio.
 */
export function exibirTelefone(e164: string | null | undefined): string {
  if (!e164) return "—";
  const d = e164.replace(/\D/g, "");
  const nacional = d.startsWith("55") && d.length >= 12 ? d.slice(2) : d;
  const fmt = formatarTelefoneBR(nacional);
  return fmt || e164;
}
