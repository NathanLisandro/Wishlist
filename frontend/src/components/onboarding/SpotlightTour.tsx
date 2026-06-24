"use client";

import { useCallback, useEffect, useState } from "react";
import { ArrowLeft, ArrowRight, Check, X } from "lucide-react";

export type TourStep = {
  /** Seletor CSS do elemento a destacar. Sem alvo, o passo é exibido centralizado. */
  target?: string;
  title: string;
  body: string;
};

type Caixa = { top: number; left: number; width: number; height: number };

const PADDING = 8; // respiro entre o elemento e o recorte do holofote
const GAP = 14; // distância entre o recorte e o card de texto
const CARD_W = 340;
const CARD_H_EST = 210; // estimativa para decidir acima/abaixo do alvo

/**
 * Tour guiado por holofote: escurece a tela, recorta o elemento em foco e mostra
 * um card com texto e navegação. Some quando concluído ou pulado.
 */
export function SpotlightTour({
  steps,
  onFinish,
  onSkip,
}: {
  steps: TourStep[];
  onFinish: () => void;
  onSkip: () => void;
}) {
  const [i, setI] = useState(0);
  const [caixa, setCaixa] = useState<Caixa | null>(null);
  const step = steps[i];
  const ultimo = i === steps.length - 1;
  const primeiro = i === 0;

  const medir = useCallback(() => {
    const alvo = step?.target ? document.querySelector(step.target) : null;
    if (!alvo) {
      setCaixa(null);
      return;
    }
    const r = alvo.getBoundingClientRect();
    setCaixa({
      top: r.top - PADDING,
      left: r.left - PADDING,
      width: r.width + PADDING * 2,
      height: r.height + PADDING * 2,
    });
  }, [step]);

  // A cada passo: rola o alvo à vista, mede (antes do paint e após o scroll
  // suave assentar) e mantém o holofote alinhado em scroll/resize.
  useEffect(() => {
    const alvo = step?.target ? document.querySelector(step.target) : null;
    alvo?.scrollIntoView({ behavior: "smooth", block: "center" });
    const raf = requestAnimationFrame(medir);
    const t = setTimeout(medir, 340);
    window.addEventListener("resize", medir);
    window.addEventListener("scroll", medir, true);
    return () => {
      cancelAnimationFrame(raf);
      clearTimeout(t);
      window.removeEventListener("resize", medir);
      window.removeEventListener("scroll", medir, true);
    };
  }, [step, medir]);

  const proximo = useCallback(() => {
    if (ultimo) onFinish();
    else setI((n) => n + 1);
  }, [ultimo, onFinish]);

  const anterior = useCallback(() => setI((n) => Math.max(0, n - 1)), []);

  // Atalhos de teclado.
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") onSkip();
      else if (e.key === "ArrowRight" || e.key === "Enter") proximo();
      else if (e.key === "ArrowLeft") anterior();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [onSkip, proximo, anterior]);

  if (!step) return null;

  // Posição do card: centralizado quando não há alvo; senão, abaixo ou acima dele.
  const vw = typeof window !== "undefined" ? window.innerWidth : 1024;
  const vh = typeof window !== "undefined" ? window.innerHeight : 768;
  const largura = Math.min(CARD_W, vw - 24);

  let cardStyle: React.CSSProperties;
  if (!caixa) {
    cardStyle = {
      width: largura,
      left: (vw - largura) / 2,
      top: Math.max(24, (vh - CARD_H_EST) / 2),
    };
  } else {
    const acima = caixa.top + caixa.height + GAP + CARD_H_EST > vh;
    const top = acima
      ? Math.max(16, caixa.top - GAP - CARD_H_EST)
      : caixa.top + caixa.height + GAP;
    const left = Math.min(
      Math.max(12, caixa.left + caixa.width / 2 - largura / 2),
      vw - largura - 12,
    );
    cardStyle = { width: largura, left, top };
  }

  return (
    <div className="fixed inset-0 z-[90]" role="dialog" aria-modal="true" aria-label={step.title}>
      {caixa ? (
        // Holofote: o box-shadow gigante escurece tudo, exceto o recorte.
        <div
          className="pointer-events-none absolute rounded-xl transition-all duration-300 ease-out"
          style={{
            top: caixa.top,
            left: caixa.left,
            width: caixa.width,
            height: caixa.height,
            boxShadow: "0 0 0 9999px rgba(20, 12, 24, 0.62)",
            outline: "2px solid rgba(255,255,255,0.85)",
            outlineOffset: 2,
          }}
        />
      ) : (
        <div className="absolute inset-0 bg-[rgba(20,12,24,0.62)]" />
      )}

      <div
        className="absolute rounded-2xl border border-lilac-line bg-card p-5 shadow-2xl transition-all duration-300 ease-out"
        style={cardStyle}
      >
        <button
          onClick={onSkip}
          aria-label="Pular tour"
          className="absolute right-3 top-3 grid size-7 cursor-pointer place-items-center rounded-lg text-muted-foreground transition-colors hover:bg-lilac hover:text-plum"
        >
          <X className="size-4" />
        </button>

        <p className="text-[11px] font-medium uppercase tracking-wide text-plum/70">
          Passo {i + 1} de {steps.length}
        </p>
        <h3 className="mt-1 font-serif text-xl leading-tight text-foreground">{step.title}</h3>
        <p className="mt-2 text-sm leading-relaxed text-muted-foreground">{step.body}</p>

        <div className="mt-5 flex items-center justify-between gap-3">
          <div className="flex gap-1.5">
            {steps.map((_, idx) => (
              <span
                key={idx}
                className={
                  "h-1.5 rounded-full transition-all " +
                  (idx === i ? "w-5 bg-plum" : "w-1.5 bg-lilac-line")
                }
              />
            ))}
          </div>
          <div className="flex items-center gap-2">
            {!primeiro && (
              <button
                onClick={anterior}
                className="flex cursor-pointer items-center gap-1 rounded-lg border border-border px-3 py-2 text-[13px] font-medium text-muted-foreground transition-colors hover:border-lilac-line hover:text-plum"
              >
                <ArrowLeft className="size-3.5" /> Voltar
              </button>
            )}
            <button
              onClick={proximo}
              className="brand-gradient flex cursor-pointer items-center gap-1.5 rounded-lg px-4 py-2 text-[13px] font-medium text-white transition-opacity hover:opacity-95"
            >
              {ultimo ? (
                <>
                  Concluir <Check className="size-3.5" />
                </>
              ) : (
                <>
                  Próximo <ArrowRight className="size-3.5" />
                </>
              )}
            </button>
          </div>
        </div>

        {!ultimo && (
          <button
            onClick={onSkip}
            className="mt-3 w-full cursor-pointer text-center text-xs text-muted-foreground/80 underline-offset-4 hover:underline"
          >
            Pular introdução
          </button>
        )}
      </div>
    </div>
  );
}
