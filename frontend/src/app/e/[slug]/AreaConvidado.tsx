"use client";

import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Check, LogOut } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { formatarTelefoneBR, apenasDigitos } from "@/lib/format";
import { getGuest, setGuest, clearGuest } from "@/lib/auth";
import type { EventoPublico, SessaoConvidado } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { PassoConfirmacao } from "./PassoConfirmacao";
import { PassoPresente } from "./PassoPresente";

export function AreaConvidado({ evento }: { evento: EventoPublico }) {
  const slug = evento.slug;
  const temConfirmacao = evento.rsvpHabilitado;
  const [sessao, setSessao] = useState<SessaoConvidado | null>(null);
  const [telefone, setTelefone] = useState("");
  const [carregando, setCarregando] = useState(false);
  const [passo, setPasso] = useState(1);

  useEffect(() => {
    setSessao(getGuest(slug));
  }, [slug]);

  useEffect(() => {
    if (!temConfirmacao) setPasso(2);
  }, [temConfirmacao]);

  async function entrar(e: React.FormEvent) {
    e.preventDefault();
    const digitos = apenasDigitos(telefone);
    if (digitos.length < 10) {
      toast.error("Informe o DDD + número (ex.: 11 99999-9999).");
      return;
    }
    setCarregando(true);
    try {
      const s = await api.post<SessaoConvidado>(
        `/public/events/${slug}/guest-session`,
        { telefone: `+55${digitos}` },
      );
      setGuest(slug, s);
      setSessao(s);
      setPasso(temConfirmacao ? 1 : 2);
      toast.success(`Olá, ${s.nomeConvidado}!`);
    } catch (err) {
      toast.error(
        err instanceof ApiError ? err.message : "Não foi possível entrar. Tente novamente.",
      );
    } finally {
      setCarregando(false);
    }
  }

  function sair() {
    clearGuest(slug);
    setSessao(null);
    setTelefone("");
    setPasso(temConfirmacao ? 1 : 2);
  }

  function sessaoInvalida() {
    clearGuest(slug);
    setSessao(null);
    setPasso(temConfirmacao ? 1 : 2);
    toast.error(
      "Sua sessão expirou ou o evento foi atualizado. Entre novamente com seu telefone.",
    );
  }

  if (!sessao) {
    return (
      <Card className="rise rounded-[22px] border-border shadow-[0_30px_60px_-34px_rgba(110,31,67,0.4)]">
        <CardContent className="space-y-5 p-6 sm:p-7">
          <div>
            <h2 className="font-serif text-2xl">Confirme sua presença</h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Identifique-se com o telefone que você informou ao casal.
            </p>
          </div>
          <form onSubmit={entrar} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="telefone">Seu telefone</Label>
              <div className="flex items-stretch overflow-hidden rounded-lg border border-input bg-white focus-within:border-ring focus-within:ring-2 focus-within:ring-ring/30">
                <span className="grid place-items-center border-r border-input bg-lilac px-3 text-sm font-medium text-plum">
                  🇧🇷 +55
                </span>
                <input
                  id="telefone"
                  type="tel"
                  inputMode="numeric"
                  className="h-12 flex-1 bg-transparent px-3 text-base outline-none"
                  placeholder="(11) 99999-9999"
                  value={telefone}
                  onChange={(e) => setTelefone(formatarTelefoneBR(e.target.value))}
                  required
                />
              </div>
              <p className="text-xs text-muted-foreground">
                Digite o <strong>DDD</strong> e o número do celular informado ao casal.
              </p>
            </div>
            <Button
              type="submit"
              className="brand-gradient h-12 w-full border-0 text-base text-white shadow-[0_14px_26px_-12px_rgba(142,43,87,0.6)] hover:opacity-95"
              disabled={carregando}
            >
              {carregando ? "Entrando..." : "Entrar"}
            </Button>
          </form>
        </CardContent>
      </Card>
    );
  }

  const iniciais = sessao.nomeConvidado
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase())
    .join("");

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between rounded-2xl border border-border bg-white/70 px-4 py-3 backdrop-blur">
        <div className="flex items-center gap-3">
          <span className="brand-gradient grid size-9 place-items-center rounded-full text-xs font-semibold text-white">
            {iniciais}
          </span>
          <p className="text-sm">
            Olá, <strong>{sessao.nomeConvidado}</strong>
          </p>
        </div>
        <button
          type="button"
          onClick={sair}
          className="flex cursor-pointer items-center gap-1.5 rounded-lg border border-border px-3 py-1.5 text-[13px] font-medium text-muted-foreground transition-colors hover:border-lilac-line hover:bg-white hover:text-plum"
        >
          <LogOut className="size-3.5" /> Sair
        </button>
      </div>

      {temConfirmacao && <Indicador passo={passo} />}

      {temConfirmacao && passo === 1 ? (
        <PassoConfirmacao
          slug={slug}
          token={sessao.token}
          acompanhantesPermitidos={sessao.acompanhantesPermitidos}
          onContinuar={() => setPasso(2)}
          onSessaoInvalida={sessaoInvalida}
        />
      ) : (
        <PassoPresente
          slug={slug}
          token={sessao.token}
          permiteDinheiro={evento.presenteDinheiroHabilitado}
          onVoltar={temConfirmacao ? () => setPasso(1) : undefined}
          onSessaoInvalida={sessaoInvalida}
        />
      )}
    </div>
  );
}

function Indicador({ passo }: { passo: number }) {
  const etapas = ["Presença", "Presente"];
  return (
    <div className="flex items-center justify-center gap-3">
      {etapas.map((label, i) => {
        const n = i + 1;
        const ativo = passo === n;
        const feito = passo > n;
        return (
          <div key={label} className="flex items-center gap-3">
            <span
              className={
                "flex items-center gap-2 text-sm transition-colors " +
                (ativo || feito ? "font-medium text-foreground" : "text-muted-foreground")
              }
            >
              <span
                className={
                  "grid size-[25px] place-items-center rounded-full text-[13px] transition-colors " +
                  (ativo || feito
                    ? "brand-gradient border border-transparent text-white"
                    : "border border-border bg-white text-muted-foreground")
                }
              >
                {feito ? <Check className="size-3.5" /> : n}
              </span>
              {label}
            </span>
            {i === 0 && <span className="h-px w-12 bg-border" />}
          </div>
        );
      })}
    </div>
  );
}
