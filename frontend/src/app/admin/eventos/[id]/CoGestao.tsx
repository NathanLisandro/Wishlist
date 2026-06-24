"use client";

import { useCallback, useEffect, useState } from "react";
import { toast } from "sonner";
import { Users, Copy, RefreshCw, Trash2, Link2, X, Gift } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import type { Colaborador } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Modal } from "@/components/Modal";

export function CoGestao({
  token,
  eventoId,
  tokenInicial,
  modoSurpresaInicial,
  onSurpresaChange,
}: {
  token: string;
  eventoId: string;
  tokenInicial: string | null;
  modoSurpresaInicial?: boolean;
  onSurpresaChange?: () => void;
}) {
  const [aberto, setAberto] = useState(false);
  const [conviteToken, setConviteToken] = useState<string | null>(tokenInicial);
  const [colaboradores, setColaboradores] = useState<Colaborador[]>([]);
  const [gerando, setGerando] = useState(false);
  const [surpresa, setSurpresa] = useState(!!modoSurpresaInicial);
  const [salvandoSurpresa, setSalvandoSurpresa] = useState(false);

  const link =
    conviteToken && typeof window !== "undefined"
      ? `${window.location.origin}/admin/convite/${conviteToken}`
      : "";

  const carregar = useCallback(async () => {
    try {
      setColaboradores(await api.get<Colaborador[]>(`/events/${eventoId}/collaborators`, token));
    } catch {
      /* silencioso */
    }
  }, [eventoId, token]);

  useEffect(() => {
    if (aberto) carregar();
  }, [aberto, carregar]);

  async function gerar() {
    setGerando(true);
    try {
      const r = await api.post<{ token: string }>(`/events/${eventoId}/manage-invite`, undefined, token);
      setConviteToken(r.token);
      toast.success("Link de co-gestão gerado!");
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro ao gerar o link");
    } finally {
      setGerando(false);
    }
  }

  async function revogar() {
    try {
      await api.del(`/events/${eventoId}/manage-invite`, token);
      setConviteToken(null);
      toast.success("Link revogado.");
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro ao revogar");
    }
  }

  async function remover(c: Colaborador) {
    try {
      await api.del(`/events/${eventoId}/collaborators/${c.contaId}`, token);
      toast.success(`${c.nomeExibicao} removido(a) da gestão.`);
      await carregar();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro ao remover");
    }
  }

  function copiar() {
    navigator.clipboard.writeText(link);
    toast.success("Link copiado!");
  }

  async function alternarSurpresa(ativo: boolean) {
    setSalvandoSurpresa(true);
    try {
      await api.put(`/events/${eventoId}/surprise`, { ativo }, token);
      setSurpresa(ativo);
      toast.success(ativo ? "Modo surpresa ligado!" : "Modo surpresa desligado.");
      onSurpresaChange?.();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro ao alterar o modo surpresa");
    } finally {
      setSalvandoSurpresa(false);
    }
  }

  return (
    <>
      <button
        onClick={() => setAberto(true)}
        className="flex items-center gap-1.5 rounded-lg border border-border px-3 py-1.5 text-[13px] font-medium text-muted-foreground transition-colors hover:border-lilac-line hover:bg-white hover:text-plum"
      >
        <Users className="size-3.5" /> Compartilhar gestão
      </button>

      <Modal open={aberto} onClose={() => setAberto(false)}>
        <div className="space-y-5">
          <div className="flex items-center gap-3">
            <span className="grid size-11 place-items-center rounded-[14px] bg-lilac text-plum">
              <Users className="size-5" />
            </span>
            <div>
              <h3 className="font-serif text-2xl">Co-gestão do evento</h3>
              <p className="text-sm text-muted-foreground">
                Convide alguém para ajudar a gerenciar (presentes, convidados, painel).
              </p>
            </div>
          </div>

          {/* Link de convite */}
          <div className="space-y-2 rounded-xl border border-lilac-line bg-lilac/50 p-4">
            <p className="flex items-center gap-1.5 text-sm font-medium text-plum">
              <Link2 className="size-4" /> Link de convite
            </p>
            {conviteToken ? (
              <>
                <div className="flex gap-2">
                  <input
                    readOnly
                    value={link}
                    className="h-10 flex-1 rounded-lg border border-input bg-white px-3 font-mono text-xs outline-none"
                  />
                  <Button variant="outline" className="h-10" onClick={copiar}>
                    <Copy className="size-3.5" /> Copiar
                  </Button>
                </div>
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    className="h-9 gap-1.5 text-muted-foreground hover:text-plum"
                    onClick={gerar}
                    disabled={gerando}
                  >
                    <RefreshCw className="size-3.5" /> Regenerar (invalida o atual)
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    className="h-9 gap-1.5 text-muted-foreground hover:border-rose hover:text-rose"
                    onClick={revogar}
                  >
                    <X className="size-3.5" /> Revogar
                  </Button>
                </div>
                <p className="text-xs text-muted-foreground">
                  Quem abrir esse link cria uma conta (ou entra) e passa a co-gerenciar este evento.
                </p>
              </>
            ) : (
              <Button
                className="brand-gradient h-10 border-0 text-white hover:opacity-95"
                onClick={gerar}
                disabled={gerando}
              >
                <Link2 className="size-4" /> {gerando ? "Gerando..." : "Gerar link de convite"}
              </Button>
            )}
          </div>

          {/* Co-gestores atuais */}
          <div className="space-y-2">
            <p className="text-sm font-medium">Co-gestores</p>
            {colaboradores.length === 0 ? (
              <p className="text-sm text-muted-foreground">Ninguém ainda além de você.</p>
            ) : (
              <ul className="divide-y divide-border rounded-xl border border-border">
                {colaboradores.map((c) => (
                  <li key={c.contaId} className="flex items-center gap-3 p-2.5">
                    <span className="grid size-8 flex-none place-items-center rounded-full bg-lilac text-xs font-semibold text-plum">
                      {c.nomeExibicao.slice(0, 2).toUpperCase()}
                    </span>
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-medium">{c.nomeExibicao}</p>
                      <p className="truncate text-xs text-muted-foreground">{c.email}</p>
                    </div>
                    <button
                      type="button"
                      aria-label="Remover co-gestor"
                      onClick={() => remover(c)}
                      className="grid cursor-pointer place-items-center rounded-lg p-2 text-muted-foreground transition-colors hover:bg-rose-50 hover:text-rose"
                    >
                      <Trash2 className="size-4" />
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          {/* Modo surpresa */}
          <button
            type="button"
            onClick={() => alternarSurpresa(!surpresa)}
            disabled={salvandoSurpresa}
            className={
              "flex w-full cursor-pointer items-center justify-between gap-3 rounded-xl border p-3 text-left transition-colors " +
              (surpresa ? "border-lilac-line bg-lilac" : "border-border bg-white")
            }
          >
            <span className="flex items-start gap-2">
              <Gift className="mt-0.5 size-4 flex-none text-plum" />
              <span>
                <span className="block text-sm font-medium">Modo surpresa</span>
                <span className="block text-xs text-muted-foreground">
                  Só o co-gestor mexe nos presentes. Você (dono) não vê a lista — para ser
                  surpreendido(a). Precisa de um co-gestor pelo link acima.
                </span>
              </span>
            </span>
            <span
              className={
                "relative h-6 w-10 flex-none rounded-full transition-colors " +
                (surpresa ? "brand-gradient" : "bg-muted-foreground/30")
              }
            >
              <span
                className={
                  "absolute top-0.5 size-5 rounded-full bg-white shadow transition-all " +
                  (surpresa ? "left-[18px]" : "left-0.5")
                }
              />
            </span>
          </button>

          <Button variant="outline" className="h-11 w-full" onClick={() => setAberto(false)}>
            Fechar
          </Button>
        </div>
      </Modal>
    </>
  );
}
