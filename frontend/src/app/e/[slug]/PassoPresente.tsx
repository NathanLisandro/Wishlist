"use client";

import { useCallback, useEffect, useState } from "react";
import { toast } from "sonner";
import {
  ArrowLeft, Banknote, Gift, Heart, ExternalLink, X, Check, Plus, ListChecks, CheckCircle2,
} from "lucide-react";
import { api, ApiError, ehSessaoInvalida, mediaUrl } from "@/lib/api";
import { formatarMoeda, moedaParaNumero } from "@/lib/format";
import type { ItemDisponivel, MoneyGiftResultado, ReservaResultado } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Modal } from "@/components/Modal";
import { GiftPlaceholder } from "@/components/GiftPlaceholder";

export function PassoPresente({
  slug,
  token,
  permiteDinheiro,
  onVoltar,
  onConcluir,
  onSessaoInvalida,
}: {
  slug: string;
  token: string;
  permiteDinheiro: boolean;
  onVoltar?: () => void;
  onConcluir: () => void;
  onSessaoInvalida: () => void;
}) {
  const [itens, setItens] = useState<ItemDisponivel[]>([]);
  const [meus, setMeus] = useState<ItemDisponivel[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [adicionandoId, setAdicionandoId] = useState<string | null>(null);
  const [cancelandoId, setCancelandoId] = useState<string | null>(null);

  const [confirmarAberto, setConfirmarAberto] = useState(false);
  const [dinheiroAberto, setDinheiroAberto] = useState(false);
  const [valor, setValor] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [enviandoDinheiro, setEnviandoDinheiro] = useState(false);

  const carregar = useCallback(async () => {
    try {
      const [disponiveis, meusItens] = await Promise.all([
        api.get<ItemDisponivel[]>(`/public/events/${slug}/gifts`, token),
        api.get<ItemDisponivel[]>(`/public/events/${slug}/my-gifts`, token),
      ]);
      setItens(disponiveis);
      setMeus(meusItens);
    } catch (err) {
      if (ehSessaoInvalida(err)) return onSessaoInvalida();
      toast.error(err instanceof ApiError ? err.message : "Erro ao carregar presentes");
    } finally {
      setCarregando(false);
    }
  }, [slug, token, onSessaoInvalida]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  async function adicionar(item: ItemDisponivel) {
    setAdicionandoId(item.id);
    try {
      await api.post<ReservaResultado>(
        `/public/events/${slug}/gifts/${item.id}/claim`,
        undefined,
        token,
        { "Idempotency-Key": crypto.randomUUID() },
      );
      toast.success(`Adicionado: ${item.nome}`);
      await carregar();
    } catch (err) {
      if (ehSessaoInvalida(err)) return onSessaoInvalida();
      toast.error(err instanceof ApiError ? err.message : "Não foi possível adicionar");
    } finally {
      setAdicionandoId(null);
    }
  }

  async function remover(item: ItemDisponivel) {
    setCancelandoId(item.id);
    try {
      await api.del(`/public/events/${slug}/gifts/${item.id}/claim`, token);
      toast.success(`Removido: ${item.nome}`);
      await carregar();
    } catch (err) {
      if (ehSessaoInvalida(err)) return onSessaoInvalida();
      toast.error(err instanceof ApiError ? err.message : "Não foi possível remover");
    } finally {
      setCancelandoId(null);
    }
  }

  async function darDinheiro() {
    const numero = moedaParaNumero(valor);
    if (!numero || numero <= 0) {
      toast.error("Informe um valor válido.");
      return;
    }
    setEnviandoDinheiro(true);
    try {
      const r = await api.post<MoneyGiftResultado>(
        `/public/events/${slug}/money-gift`,
        { valor: numero, mensagem: mensagem || null },
        token,
      );
      toast.success("Redirecionando para o pagamento...");
      window.location.href = r.initPoint;
    } catch (err) {
      if (ehSessaoInvalida(err)) return onSessaoInvalida();
      toast.error(err instanceof ApiError ? err.message : "Não foi possível iniciar o pagamento");
      setEnviandoDinheiro(false);
    }
  }

  // Na lista de escolha não repetimos o que já está em "Meus presentes".
  const disponiveis = itens.filter((i) => !i.reservadoPorMim);

  const passos = [
    { Icon: Plus, texto: "Toque em Adicionar nos presentes que quiser" },
    { Icon: ListChecks, texto: "Revise sua lista em “Meus presentes”" },
    { Icon: Check, texto: "Toque em Confirmar para finalizar" },
  ];

  return (
    <div className="space-y-5 pb-24">
      {onVoltar && (
        <button
          type="button"
          onClick={onVoltar}
          className="flex cursor-pointer items-center gap-1.5 text-sm text-muted-foreground transition-colors hover:text-plum"
        >
          <ArrowLeft className="size-4" /> Voltar para a confirmação
        </button>
      )}

      <div className="flex items-start gap-3.5">
        <span className="grid size-11 flex-none place-items-center rounded-[14px] bg-lilac text-plum">
          <Gift className="size-[22px]" />
        </span>
        <div>
          <h2 className="font-serif text-[26px] leading-tight">Escolha seus presentes</h2>
          <p className="mt-1 text-sm text-muted-foreground">
            Você pode escolher <strong>mais de um</strong> — e trocar quando quiser.
          </p>
        </div>
      </div>

      {/* Passo a passo sempre visível (ícones informativos). */}
      <div className="grid gap-2 rounded-2xl border border-lilac-line bg-lilac/50 p-3 sm:grid-cols-3">
        {passos.map((p, i) => (
          <div key={i} className="flex items-center gap-2.5">
            <span className="grid size-8 flex-none place-items-center rounded-full bg-white text-plum shadow-sm">
              <p.Icon className="size-4" />
            </span>
            <span className="text-[13px] leading-tight text-plum/90">
              <strong className="font-medium">{i + 1}.</strong> {p.texto}
            </span>
          </div>
        ))}
      </div>

      {/* Meus presentes — o que o convidado já escolheu (removível). */}
      {meus.length > 0 && (
        <div className="rounded-2xl border border-lilac-line bg-lilac p-4">
          <div className="mb-3 flex items-center gap-2 text-[13px] font-medium text-plum">
            <Heart className="size-4" /> Meus presentes ({meus.length})
          </div>
          <div className="space-y-2">
            {meus.map((item) => (
              <div
                key={item.id}
                className="flex items-center gap-3 rounded-xl border border-border bg-white p-2"
              >
                <Thumb url={item.imagemUrl} />
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium">{item.nome}</p>
                  {item.variante && (
                    <p className="truncate text-xs text-muted-foreground">{item.variante}</p>
                  )}
                </div>
                <button
                  type="button"
                  onClick={() => remover(item)}
                  disabled={cancelandoId === item.id}
                  className="flex cursor-pointer items-center gap-1.5 rounded-lg border border-border px-3 py-1.5 text-[13px] font-medium text-muted-foreground transition-colors hover:border-rose hover:text-rose disabled:opacity-50"
                >
                  <X className="size-3.5" />
                  {cancelandoId === item.id ? "..." : "Remover"}
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="grid gap-3 sm:grid-cols-2">
        {permiteDinheiro && (
          <div className="flex flex-col justify-between gap-3 rounded-2xl border border-emerald-200 bg-emerald-50/70 p-4">
            <div className="flex items-start gap-3">
              <span className="grid size-11 flex-none place-items-center rounded-xl bg-emerald-100 text-emerald-700">
                <Banknote className="size-6" />
              </span>
              <div>
                <h3 className="font-medium">Presentear com dinheiro</h3>
                <p className="text-sm text-muted-foreground">
                  Presenteie com um valor à vontade via Mercado Pago.
                </p>
              </div>
            </div>
            <Button
              className="h-11 w-full border-0 bg-emerald-600 text-base text-white hover:bg-emerald-700"
              onClick={() => setDinheiroAberto(true)}
            >
              Presentear com dinheiro
            </Button>
          </div>
        )}

        {!carregando &&
          disponiveis.map((item) => {
            const adicionando = adicionandoId === item.id;
            return (
              <div
                key={item.id}
                className="flex flex-col overflow-hidden rounded-2xl border border-border bg-white shadow-[0_16px_40px_-30px_rgba(110,31,67,0.45)]"
              >
                <div className="relative aspect-[4/3] w-full overflow-hidden bg-lilac">
                  {mediaUrl(item.imagemUrl) ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img
                      src={mediaUrl(item.imagemUrl)!}
                      alt={item.nome}
                      className="size-full object-cover"
                    />
                  ) : (
                    <GiftPlaceholder className="size-full" />
                  )}
                  <span className="absolute right-2 top-2 rounded-full bg-white/85 px-2.5 py-0.5 text-xs font-medium text-plum backdrop-blur">
                    {item.vagasRestantes} restante(s)
                  </span>
                </div>
                <div className="flex flex-1 flex-col gap-2 p-4">
                  <div className="flex-1 space-y-1">
                    <h3 className="font-medium leading-snug">{item.nome}</h3>
                    {item.variante && (
                      <p className="text-sm text-muted-foreground">{item.variante}</p>
                    )}
                    {item.linkProduto && (
                      <a
                        href={item.linkProduto}
                        target="_blank"
                        rel="noreferrer"
                        className="inline-flex items-center gap-1 text-sm text-rose transition-colors hover:text-plum"
                      >
                        Ver produto <ExternalLink className="size-3" />
                      </a>
                    )}
                  </div>
                  <Button
                    className="brand-gradient h-11 w-full border-0 text-base text-white hover:opacity-95"
                    disabled={item.vagasRestantes <= 0 || adicionando}
                    onClick={() => adicionar(item)}
                  >
                    <Plus className="size-4" /> {adicionando ? "Adicionando..." : "Adicionar"}
                  </Button>
                </div>
              </div>
            );
          })}
      </div>

      {carregando && (
        <p className="py-6 text-center text-muted-foreground">Carregando presentes...</p>
      )}
      {!carregando && disponiveis.length === 0 && meus.length === 0 && !permiteDinheiro && (
        <p className="py-6 text-center text-muted-foreground">
          Nenhum presente disponível no momento.
        </p>
      )}

      {/* Barra fixa de confirmação — sempre visível quando há presentes escolhidos. */}
      {meus.length > 0 && (
        <div className="fixed inset-x-0 bottom-0 z-40 border-t border-lilac-line bg-white/85 backdrop-blur">
          <div className="mx-auto flex max-w-[600px] items-center gap-3 px-5 py-3 sm:px-6">
            <p className="flex-1 text-sm">
              <strong>{meus.length}</strong> presente(s) escolhido(s)
            </p>
            <Button
              className="brand-gradient h-11 border-0 px-6 text-base text-white shadow-[0_14px_26px_-12px_rgba(142,43,87,0.6)] hover:opacity-95"
              onClick={() => setConfirmarAberto(true)}
            >
              <Check className="size-4" /> Confirmar
            </Button>
          </div>
        </div>
      )}

      {/* Modal de confirmação final */}
      <Modal open={confirmarAberto} onClose={() => setConfirmarAberto(false)}>
        <div className="space-y-4 text-center">
          <div className="brand-gradient mx-auto grid size-12 place-items-center rounded-2xl text-white">
            <CheckCircle2 className="size-6" />
          </div>
          <div>
            <h3 className="font-serif text-2xl">Confirmar sua escolha</h3>
            <p className="mt-1 text-sm text-muted-foreground">
              Você escolheu <strong>{meus.length}</strong> presente(s) para o casal. Podemos
              confirmar?
            </p>
          </div>
          <div className="flex gap-2">
            <Button
              variant="outline"
              className="h-11 flex-1"
              onClick={() => setConfirmarAberto(false)}
            >
              Revisar
            </Button>
            <Button
              className="brand-gradient h-11 flex-1 border-0 text-white hover:opacity-95"
              onClick={() => {
                setConfirmarAberto(false);
                onConcluir();
              }}
            >
              <Check className="size-4" /> Confirmar
            </Button>
          </div>
        </div>
      </Modal>

      {/* Modal de dinheiro */}
      <Modal open={dinheiroAberto} onClose={() => !enviandoDinheiro && setDinheiroAberto(false)}>
        <div className="space-y-4">
          <div className="flex items-center gap-3">
            <span className="grid size-12 place-items-center rounded-2xl bg-emerald-100 text-emerald-700">
              <Banknote className="size-6" />
            </span>
            <div>
              <h3 className="font-serif text-2xl">Presentear com dinheiro</h3>
              <p className="text-sm text-muted-foreground">Pagamento seguro via Mercado Pago.</p>
            </div>
          </div>
          <div className="space-y-2">
            <Label htmlFor="valor">Valor</Label>
            <div className="flex items-stretch overflow-hidden rounded-lg border border-input bg-white focus-within:border-ring focus-within:ring-2 focus-within:ring-ring/30">
              <span className="grid place-items-center border-r border-input bg-emerald-50 px-3 text-base font-medium text-emerald-700">
                R$
              </span>
              <input
                id="valor"
                inputMode="numeric"
                className="h-12 flex-1 bg-transparent px-3 text-base outline-none"
                placeholder="0,00"
                value={valor}
                onChange={(e) => setValor(formatarMoeda(e.target.value))}
              />
            </div>
          </div>
          <div className="space-y-2">
            <Label htmlFor="msg">Recado ao casal (opcional)</Label>
            <Textarea
              id="msg"
              value={mensagem}
              maxLength={500}
              onChange={(e) => setMensagem(e.target.value)}
            />
          </div>
          <div className="flex gap-2">
            <Button
              variant="outline"
              className="h-11 flex-1"
              onClick={() => setDinheiroAberto(false)}
              disabled={enviandoDinheiro}
            >
              Cancelar
            </Button>
            <Button
              className="h-11 flex-1 border-0 bg-emerald-600 text-white hover:bg-emerald-700"
              onClick={darDinheiro}
              disabled={enviandoDinheiro}
            >
              {enviandoDinheiro ? "Aguarde..." : "Confirmar e pagar"}
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}

function Thumb({ url }: { url: string | null }) {
  const src = mediaUrl(url);
  return (
    <span className="grid size-11 flex-none place-items-center overflow-hidden rounded-lg">
      {src ? (
        // eslint-disable-next-line @next/next/no-img-element
        <img src={src} alt="" className="size-full object-cover" />
      ) : (
        <GiftPlaceholder className="size-full rounded-lg" />
      )}
    </span>
  );
}
