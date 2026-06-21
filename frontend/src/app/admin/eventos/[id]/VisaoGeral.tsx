"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import {
  Users,
  CalendarCheck,
  Gift,
  Banknote,
  Wallet,
  Activity,
} from "lucide-react";
import { api, ApiError } from "@/lib/api";
import type { Dashboard } from "@/lib/types";
import { Card, CardContent } from "@/components/ui/card";

const ACOES: Record<string, { label: string; cor: string }> = {
  LOGIN_SUCCESS: { label: "Entrou", cor: "text-emerald-600" },
  LOGIN_FAILED: { label: "Tentativa de entrada", cor: "text-amber-600" },
  LIST_GIFTS: { label: "Viu os presentes", cor: "text-plum" },
  CLAIM_GIFT: { label: "Escolheu um presente", cor: "text-rose" },
  CANCEL_CLAIM: { label: "Trocou um presente", cor: "text-muted-foreground" },
  MONEY_GIFT: { label: "Deu dinheiro", cor: "text-emerald-600" },
  RSVP_CONFIRM: { label: "Confirmou presença", cor: "text-emerald-600" },
  RSVP_CANCEL: { label: "Marcou que não vai", cor: "text-muted-foreground" },
};

function tempoRelativo(iso: string) {
  const diff = Date.now() - new Date(iso).getTime();
  const min = Math.floor(diff / 60000);
  if (min < 1) return "agora mesmo";
  if (min < 60) return `há ${min} min`;
  const h = Math.floor(min / 60);
  if (h < 24) return `há ${h} h`;
  return new Date(iso).toLocaleDateString("pt-BR");
}

export function VisaoGeral({
  token,
  eventoId,
  onExpirado,
}: {
  token: string;
  eventoId: string;
  onExpirado: () => void;
}) {
  const [dash, setDash] = useState<Dashboard | null>(null);
  const [aoVivo, setAoVivo] = useState(true);
  const aoVivoRef = useRef(aoVivo);
  aoVivoRef.current = aoVivo;

  const carregar = useCallback(async () => {
    try {
      setDash(await api.get<Dashboard>(`/events/${eventoId}/dashboard`, token));
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) onExpirado();
    }
  }, [eventoId, token, onExpirado]);

  useEffect(() => {
    carregar();
    const t = setInterval(() => {
      if (aoVivoRef.current) carregar();
    }, 4000);
    return () => clearInterval(t);
  }, [carregar]);

  if (!dash) return <p className="py-8 text-center text-muted-foreground">Carregando painel...</p>;

  const cards = [
    { icon: Users, label: "Convidados", valor: dash.convidados, cor: "bg-lilac text-plum" },
    {
      icon: CalendarCheck,
      label: "Confirmados (c/ acompanhantes)",
      valor: dash.totalPessoasConfirmadas,
      cor: "bg-emerald-50 text-emerald-600",
    },
    { icon: Gift, label: "Presentes reservados", valor: dash.presentesReservados, cor: "bg-rose-50 text-rose" },
    {
      icon: Banknote,
      label: "Presentes em dinheiro",
      valor: dash.presentesDinheiroAprovados,
      cor: "bg-emerald-50 text-emerald-600",
    },
    {
      icon: Wallet,
      label: "Dinheiro recebido",
      valor: dash.dinheiroRecebido.toLocaleString("pt-BR", { style: "currency", currency: "BRL" }),
      cor: "bg-emerald-50 text-emerald-600",
    },
    {
      icon: Users,
      label: "Não vão poder ir",
      valor: dash.recusados,
      cor: "bg-muted text-muted-foreground",
    },
  ];

  return (
    <div className="space-y-5">
      <div className="grid gap-3 sm:grid-cols-3">
        {cards.map((c) => (
          <Card key={c.label} className="rounded-2xl border-border">
            <CardContent className="p-5">
              <span className={`grid size-10 place-items-center rounded-xl ${c.cor}`}>
                <c.icon className="size-5" />
              </span>
              <p className="mt-3 text-2xl font-semibold">{c.valor}</p>
              <p className="text-sm text-muted-foreground">{c.label}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      <Card className="rounded-2xl border-border">
        <CardContent className="p-5 sm:p-6">
          <div className="mb-4 flex items-center justify-between">
            <div className="flex items-center gap-2 font-medium">
              <Activity className="size-4 text-plum" /> Quem está acessando
            </div>
            <button
              type="button"
              onClick={() => setAoVivo((v) => !v)}
              className="flex cursor-pointer items-center gap-1.5 rounded-full border border-border px-2.5 py-1 text-xs font-medium text-muted-foreground transition-colors hover:text-plum"
              title={aoVivo ? "Pausar atualização ao vivo" : "Retomar atualização ao vivo"}
            >
              <span
                className={
                  "size-2 rounded-full " +
                  (aoVivo ? "animate-pulse bg-emerald-500" : "bg-muted-foreground/40")
                }
              />
              {aoVivo ? "Ao vivo" : "Pausado"}
            </button>
          </div>

          {dash.acessosRecentes.length === 0 ? (
            <p className="py-6 text-center text-sm text-muted-foreground">
              Nenhum acesso ainda. Compartilhe o link com seus convidados ✨
            </p>
          ) : (
            <ul className="divide-y divide-border">
              {dash.acessosRecentes.map((a, i) => {
                const acao = ACOES[a.acao] ?? { label: a.acao, cor: "text-muted-foreground" };
                return (
                  <li key={i} className="flex items-center gap-3 py-2.5">
                    <span className="grid size-9 flex-none place-items-center rounded-full bg-lilac text-xs font-semibold text-plum">
                      {(a.nomeConvidado ?? "?")
                        .split(" ")
                        .filter(Boolean)
                        .slice(0, 2)
                        .map((p) => p[0]?.toUpperCase())
                        .join("") || "?"}
                    </span>
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-medium">{a.nomeConvidado ?? "Visitante"}</p>
                      <p className={`truncate text-xs ${acao.cor}`}>{acao.label}</p>
                    </div>
                    <span className="flex-none text-xs text-muted-foreground">
                      {tempoRelativo(a.quando)}
                    </span>
                  </li>
                );
              })}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
