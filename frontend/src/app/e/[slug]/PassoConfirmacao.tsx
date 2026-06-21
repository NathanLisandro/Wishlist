"use client";

import { useCallback, useEffect, useState } from "react";
import { toast } from "sonner";
import { CalendarCheck, Plus, X, Check, CalendarX, ArrowRight } from "lucide-react";
import { api, ApiError, ehSessaoInvalida } from "@/lib/api";
import type { RsvpAtual } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent } from "@/components/ui/card";

export function PassoConfirmacao({
  slug,
  token,
  acompanhantesPermitidos,
  onContinuar,
  onSessaoInvalida,
}: {
  slug: string;
  token: string;
  acompanhantesPermitidos: number;
  onContinuar: () => void;
  onSessaoInvalida: () => void;
}) {
  const [status, setStatus] = useState<RsvpAtual["status"] | null>(null);
  const [acompanhantes, setAcompanhantes] = useState<string[]>([]);
  const [editando, setEditando] = useState(false);
  const [enviando, setEnviando] = useState(false);

  const carregar = useCallback(async () => {
    try {
      const r = await api.get<RsvpAtual>(`/public/events/${slug}/rsvp`, token);
      setStatus(r.status);
      setAcompanhantes(r.acompanhantes ?? []);
    } catch (err) {
      if (ehSessaoInvalida(err)) return onSessaoInvalida();
      setStatus("PENDING");
    }
  }, [slug, token, onSessaoInvalida]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  function adicionar() {
    if (acompanhantes.length < acompanhantesPermitidos)
      setAcompanhantes([...acompanhantes, ""]);
  }
  function remover(i: number) {
    setAcompanhantes(acompanhantes.filter((_, idx) => idx !== i));
  }
  function alterar(i: number, v: string) {
    setAcompanhantes(acompanhantes.map((a, idx) => (idx === i ? v : a)));
  }

  async function confirmar() {
    const nomes = acompanhantes.map((a) => a.trim()).filter(Boolean);
    setEnviando(true);
    try {
      const r = await api.post<{ totalPessoas: number }>(
        `/public/events/${slug}/rsvp`,
        { acompanhantes: nomes },
        token,
      );
      toast.success(`Presença confirmada! Total: ${r.totalPessoas} pessoa(s).`);
      setStatus("CONFIRMED");
      setEditando(false);
    } catch (err) {
      if (ehSessaoInvalida(err)) return onSessaoInvalida();
      toast.error(err instanceof ApiError ? err.message : "Não foi possível confirmar");
    } finally {
      setEnviando(false);
    }
  }

  async function naoVou() {
    setEnviando(true);
    try {
      await api.del(`/public/events/${slug}/rsvp`, token);
      setStatus("CANCELLED");
      setAcompanhantes([]);
      setEditando(false);
      toast.success("Tudo bem, registramos que você não poderá ir.");
    } catch (err) {
      if (ehSessaoInvalida(err)) return onSessaoInvalida();
      toast.error(err instanceof ApiError ? err.message : "Erro ao registrar");
    } finally {
      setEnviando(false);
    }
  }

  const naoVai = status === "CANCELLED" || status === "DECLINED";
  const mostrarForm = editando || status === "PENDING";

  return (
    <Card className="rise rounded-[22px] border-border shadow-[0_30px_60px_-34px_rgba(110,31,67,0.4)]">
      <CardContent className="space-y-5 p-6 sm:p-7">
        <div className="flex items-start gap-3.5">
          <span className="grid size-11 flex-none place-items-center rounded-[14px] bg-lilac text-plum">
            <CalendarCheck className="size-[22px]" />
          </span>
          <div>
            <h2 className="font-serif text-[26px] leading-tight">Você vai ao evento?</h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Confirme sua presença para o casal se organizar.
            </p>
          </div>
        </div>

        {status === null ? (
          <p className="py-4 text-center text-sm text-muted-foreground">Carregando…</p>
        ) : status === "CONFIRMED" && !mostrarForm ? (
          /* Já confirmou — não deixa confirmar de novo; só permite mudar para "não vou". */
          <>
            <div className="flex items-start gap-3 rounded-2xl border border-emerald-200 bg-emerald-50/70 p-4">
              <span className="grid size-9 flex-none place-items-center rounded-full bg-emerald-100 text-emerald-700">
                <Check className="size-5" />
              </span>
              <div className="text-sm">
                <p className="font-medium text-emerald-800">Presença confirmada!</p>
                {acompanhantes.length > 0 ? (
                  <p className="text-emerald-700/90">
                    Com {acompanhantes.length} acompanhante(s): {acompanhantes.join(", ")}.
                  </p>
                ) : (
                  <p className="text-emerald-700/90">Sem acompanhantes.</p>
                )}
              </div>
            </div>
            <div className="space-y-2.5">
              <Button
                className="brand-gradient h-12 w-full border-0 text-base text-white shadow-[0_14px_26px_-12px_rgba(142,43,87,0.6)] hover:opacity-95"
                onClick={onContinuar}
              >
                Ver presentes <ArrowRight className="size-4" />
              </Button>
              <Button
                variant="outline"
                className="h-11 w-full text-muted-foreground hover:text-plum"
                onClick={naoVou}
                disabled={enviando}
              >
                Mudei de ideia — não vou poder ir
              </Button>
            </div>
          </>
        ) : naoVai && !mostrarForm ? (
          /* Marcou que não vai — permite voltar atrás e confirmar. */
          <>
            <div className="flex items-start gap-3 rounded-2xl border border-border bg-muted/40 p-4">
              <span className="grid size-9 flex-none place-items-center rounded-full bg-muted text-muted-foreground">
                <CalendarX className="size-5" />
              </span>
              <div className="text-sm">
                <p className="font-medium">Você marcou que não poderá ir.</p>
                <p className="text-muted-foreground">Mudou de ideia? É só confirmar abaixo.</p>
              </div>
            </div>
            <Button
              className="brand-gradient h-12 w-full border-0 text-base text-white shadow-[0_14px_26px_-12px_rgba(142,43,87,0.6)] hover:opacity-95"
              onClick={() => setEditando(true)}
            >
              Quero confirmar minha presença
            </Button>
            <button
              type="button"
              onClick={onContinuar}
              className="mx-auto block w-fit cursor-pointer border-b border-transparent text-sm text-muted-foreground transition-colors hover:border-rose hover:text-plum"
            >
              Pular para os presentes →
            </button>
          </>
        ) : (
          /* Formulário de confirmação (PENDING ou editando após "não vou"). */
          <>
            {acompanhantesPermitidos > 0 && (
              <div className="rounded-2xl border border-lilac-line bg-lilac p-4">
                <div className="mb-3 flex items-center justify-between">
                  <span className="text-[13px] font-medium text-plum">Acompanhantes</span>
                  <span className="rounded-full border border-lilac-line bg-white px-2.5 py-0.5 text-xs font-medium text-plum">
                    {acompanhantes.length} de {acompanhantesPermitidos}
                  </span>
                </div>

                <div className="mb-3 space-y-2">
                  {acompanhantes.map((nome, i) => (
                    <div
                      key={i}
                      className="flex items-center gap-2 rounded-xl border border-border bg-white p-1.5 pl-3"
                    >
                      <Input
                        aria-label={`Acompanhante ${i + 1}`}
                        className="h-8 flex-1 border-0 bg-transparent px-0 shadow-none focus-visible:ring-0"
                        value={nome}
                        onChange={(e) => alterar(i, e.target.value)}
                        placeholder={`Acompanhante ${i + 1} — nome completo`}
                      />
                      <button
                        type="button"
                        aria-label="Remover acompanhante"
                        onClick={() => remover(i)}
                        className="grid cursor-pointer place-items-center rounded-lg p-1.5 text-muted-foreground transition-colors hover:bg-lilac hover:text-plum"
                      >
                        <X className="size-4" />
                      </button>
                    </div>
                  ))}
                </div>

                {acompanhantes.length < acompanhantesPermitidos && (
                  <button
                    type="button"
                    onClick={adicionar}
                    className="flex w-full cursor-pointer items-center justify-center gap-2 rounded-xl border border-dashed border-rose bg-white py-2.5 text-sm font-medium text-plum transition-colors hover:bg-lilac"
                  >
                    <Plus className="size-4" /> Adicionar acompanhante
                  </button>
                )}
              </div>
            )}

            <div className="space-y-2.5">
              <Button
                className="brand-gradient h-12 w-full border-0 text-base text-white shadow-[0_14px_26px_-12px_rgba(142,43,87,0.6)] hover:opacity-95"
                onClick={confirmar}
                disabled={enviando}
              >
                Confirmar presença
              </Button>
              {status === "PENDING" && (
                <Button
                  variant="outline"
                  className="h-12 w-full text-base text-muted-foreground hover:text-plum"
                  onClick={naoVou}
                  disabled={enviando}
                >
                  Não vou poder ir
                </Button>
              )}
              {editando && (
                <button
                  type="button"
                  onClick={() => setEditando(false)}
                  className="mx-auto block w-fit cursor-pointer text-sm text-muted-foreground hover:text-plum"
                >
                  Cancelar
                </button>
              )}
            </div>

            <button
              type="button"
              onClick={onContinuar}
              className="mx-auto block w-fit cursor-pointer border-b border-transparent text-sm text-muted-foreground transition-colors hover:border-rose hover:text-plum"
            >
              Pular para os presentes →
            </button>
          </>
        )}
      </CardContent>
    </Card>
  );
}
