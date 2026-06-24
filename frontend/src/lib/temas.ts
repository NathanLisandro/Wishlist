// Paletas pré-definidas do site do casal. Espelha a whitelist PaletaTema do backend.
// Cada paleta define as CSS variables de marca; aplicadas inline no wrapper da página
// pública (SSR), sobrepõem o :root de globals.css sem flash de tema errado.
import type { CSSProperties } from "react";

type PaletaVars = {
  rose: string;
  plum: string;
  plumDeep: string;
  lilacTint: string;
  lilacLine: string;
};

const PALETAS: Record<string, PaletaVars> = {
  ROSE: { rose: "#c7407a", plum: "#8e2b57", plumDeep: "#6e1f43", lilacTint: "#f5f0fb", lilacLine: "#e8dff2" },
  AZUL: { rose: "#3b82c4", plum: "#1e4e79", plumDeep: "#16385a", lilacTint: "#eef4fb", lilacLine: "#dbe7f3" },
  VERDE: { rose: "#3a9d6e", plum: "#1f6b46", plumDeep: "#154e32", lilacTint: "#eef7f1", lilacLine: "#d7ebdf" },
  DOURADO: { rose: "#c79a3a", plum: "#8a6420", plumDeep: "#654916", lilacTint: "#faf5e9", lilacLine: "#efe4cb" },
  GRAFITE: { rose: "#6b7280", plum: "#374151", plumDeep: "#1f2937", lilacTint: "#f3f4f6", lilacLine: "#e2e5ea" },
};

const PADRAO = "ROSE";

/** Overlay padrão (%) aplicado sobre a imagem de fundo, quando o casal não escolheu. */
export const OVERLAY_PADRAO = 40;

/** Opções exibidas no seletor de paleta do admin (id + rótulo + amostra de cores). */
export const PALETAS_DISPONIVEIS: { id: string; nome: string; rose: string; plum: string }[] = [
  { id: "ROSE", nome: "Rosé", rose: PALETAS.ROSE.rose, plum: PALETAS.ROSE.plum },
  { id: "AZUL", nome: "Azul", rose: PALETAS.AZUL.rose, plum: PALETAS.AZUL.plum },
  { id: "VERDE", nome: "Verde", rose: PALETAS.VERDE.rose, plum: PALETAS.VERDE.plum },
  { id: "DOURADO", nome: "Dourado", rose: PALETAS.DOURADO.rose, plum: PALETAS.DOURADO.plum },
  { id: "GRAFITE", nome: "Grafite", rose: PALETAS.GRAFITE.rose, plum: PALETAS.GRAFITE.plum },
];

/**
 * CSS variables da paleta (com a cor de destaque opcional sobrepondo o highlight).
 * Aplicar via `style` no wrapper da página pública para tema dinâmico no SSR.
 */
export function varsDoTema(paleta: string | null, corDestaque: string | null): CSSProperties {
  const base = PALETAS[paleta ?? PADRAO] ?? PALETAS[PADRAO];
  const destaque = corDestaque || base.rose;
  return {
    "--rose": destaque,
    "--plum": base.plum,
    "--plum-deep": base.plumDeep,
    "--lilac-tint": base.lilacTint,
    "--lilac-line": base.lilacLine,
    "--primary": base.plum,
    "--secondary": base.lilacTint,
    "--muted": base.lilacTint,
    "--accent": base.lilacTint,
    "--border": base.lilacLine,
    "--input": base.lilacLine,
    "--ring": destaque,
  } as CSSProperties;
}
