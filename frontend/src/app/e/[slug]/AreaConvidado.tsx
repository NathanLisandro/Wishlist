"use client";

import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Check, LogOut, CalendarCheck, Gift, Heart } from "lucide-react";
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

      <Indicador passo={passo} temConfirmacao={temConfirmacao} />

      {passo === 3 ? (
        <TelaAgradecimento
          mensagem={evento.mensagemAgradecimento}
          onVoltar={() => setPasso(2)}
        />
      ) : temConfirmacao && passo === 1 ? (
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
          onConcluir={() => setPasso(3)}
          onSessaoInvalida={sessaoInvalida}
        />
      )}
    </div>
  );
}

function TelaAgradecimento({
  mensagem,
  onVoltar,
}: {
  mensagem: string | null;
  onVoltar: () => void;
}) {
  return (
    <Card className="rise rounded-[22px] border-border text-center shadow-[0_30px_60px_-34px_rgba(110,31,67,0.4)]">
      <CardContent className="space-y-5 p-7 sm:p-8">
        <span className="brand-gradient mx-auto grid size-16 place-items-center rounded-3xl text-white shadow-[0_18px_40px_-16px_rgba(142,43,87,0.6)]">
          <Heart className="size-8" />
        </span>
        <div>
          <h2 className="font-serif text-3xl">Tudo certo!</h2>
          <p className="mx-auto mt-2 max-w-[420px] font-serif text-lg italic leading-relaxed text-foreground">
            {mensagem?.trim() ||
              "Muito obrigado por fazer parte do nosso dia! Seu carinho já é o nosso maior presente. 💕"}
          </p>
        </div>
        <Button
          variant="outline"
          className="h-11 w-full text-muted-foreground hover:text-plum"
          onClick={onVoltar}
        >
          <Gift className="size-4" /> Ver / escolher mais presentes
        </Button>
      </CardContent>
    </Card>
  );
}

type Etapa = { n: number; label: string; Icon: typeof Check; dica: string };

function Indicador({ passo, temConfirmacao }: { passo: number; temConfirmacao: boolean }) {
  const todas: Etapa[] = [
    { n: 1, label: "Presença", Icon: CalendarCheck, dica: "Confirme se você vai ao evento." },
    { n: 2, label: "Presentes", Icon: Gift, dica: "Adicione os presentes e toque em Confirmar." },
    { n: 3, label: "Pronto", Icon: Heart, dica: "Tudo certo — obrigado!" },
  ];
  const etapas = temConfirmacao ? todas : todas.filter((e) => e.n >= 2);
  const dica = todas.find((e) => e.n === passo)?.dica;

  return (
    <div className="space-y-2.5">
      <div className="flex items-center justify-center">
        {etapas.map((e, i) => {
          const ativo = passo === e.n;
          const feito = passo > e.n;
          return (
            <div key={e.n} className="flex items-center">
              <div className="flex flex-col items-center gap-1.5">
                <span
                  className={
                    "grid size-9 place-items-center rounded-full border transition-colors " +
                    (ativo || feito
                      ? "brand-gradient border-transparent text-white"
                      : "border-border bg-white text-muted-foreground")
                  }
                >
                  {feito ? <Check className="size-4" /> : <e.Icon className="size-[18px]" />}
                </span>
                <span
                  className={
                    "text-xs transition-colors " +
                    (ativo || feito ? "font-medium text-foreground" : "text-muted-foreground")
                  }
                >
                  {e.label}
                </span>
              </div>
              {i < etapas.length - 1 && (
                <span
                  className={
                    "mx-1.5 mb-5 h-px w-10 transition-colors sm:w-16 " +
                    (passo > e.n ? "bg-rose" : "bg-border")
                  }
                />
              )}
            </div>
          );
        })}
      </div>
      {dica && <p className="text-center text-sm text-muted-foreground">{dica}</p>}
    </div>
  );
}
