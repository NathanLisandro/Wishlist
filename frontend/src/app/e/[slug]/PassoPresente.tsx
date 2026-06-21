"use client";

import { useCallback, useEffect, useState } from "react";
import { toast } from "sonner";
import { ArrowLeft, Banknote, Gift, Heart, ExternalLink, X, Check, Info } from "lucide-react";
import { api, ApiError, ehSessaoInvalida, mediaUrl } from "@/lib/api";
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
  onSessaoInvalida,
}: {
  slug: string;
  token: string;
  permiteDinheiro: boolean;
  onVoltar?: () => void;
  onSessaoInvalida: () => void;
}) {
  const [itens, setItens] = useState<ItemDisponivel[]>([]);
  const [meus, setMeus] = useState<ItemDisponivel[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [reservando, setReservando] = useState(false);
  const [cancelandoId, setCancelandoId] = useState<string | null>(null);

  const [itemConfirmar, setItemConfirmar] = useState<ItemDisponivel | null>(null);
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

  async function reservar() {
    if (!itemConfirmar) return;
    setReservando(true);
    try {
      await api.post<ReservaResultado>(
        `/public/events/${slug}/gifts/${itemConfirmar.id}/claim`,
        undefined,
        token,
        { "Idempotency-Key": crypto.randomUUID() },
      );
      toast.success(`Que presente lindo! Você escolheu: ${itemConfirmar.nome}`);
      setItemConfirmar(null);
      await carregar();
    } catch (err) {
      if (ehSessaoInvalida(err)) return onSessaoInvalida();
      toast.error(err instanceof ApiError ? err.message : "Não foi possível reservar");
    } finally {
      setReservando(false);
    }
  }

  async function cancelar(item: ItemDisponivel) {
    setCancelandoId(item.id);
    try {
      await api.del(`/public/events/${slug}/gifts/${item.id}/claim`, token);
      toast.success(`Você liberou: ${item.nome}`);
      await carregar();
    } catch (err) {
      if (ehSessaoInvalida(err)) return onSessaoInvalida();
      toast.error(err instanceof ApiError ? err.message : "Não foi possível cancelar");
    } finally {
      setCancelandoId(null);
    }
  }

  async function darDinheiro() {
    const numero = Number(valor.replace(",", "."));
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

  // Na lista de escolha não repetimos o que o convidado já escolheu (fica em "Meus presentes").
  const disponiveis = itens.filter((i) => !i.reservadoPorMim);

  return (
    <div className="space-y-5">
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
          <h2 className="font-serif text-[26px] leading-tight">Escolha um presente</h2>
          <p className="mt-1 text-sm text-muted-foreground">
            Você pode escolher <strong>mais de um</strong> — e trocar quando quiser.
          </p>
        </div>
      </div>

      <div className="flex items-start gap-2.5 rounded-xl border border-lilac-line bg-lilac/60 p-3 text-sm text-plum">
        <Info className="mt-0.5 size-4 flex-none" />
        <p>
          Presentes que já foram escolhidos por outra pessoa saem da lista
          automaticamente — assim ninguém presenteia a mesma coisa.
        </p>
      </div>

      {/* Meus presentes — o que o convidado já escolheu (cancelável). */}
      {meus.length > 0 && (
        <div className="rounded-2xl border border-lilac-line bg-lilac p-4">
          <div className="mb-3 flex items-center gap-2 text-[13px] font-medium text-plum">
            <Heart className="size-4" /> Você já escolheu {meus.length} presente(s)
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
                  onClick={() => cancelar(item)}
                  disabled={cancelandoId === item.id}
                  className="flex cursor-pointer items-center gap-1.5 rounded-lg border border-border px-3 py-1.5 text-[13px] font-medium text-muted-foreground transition-colors hover:border-rose hover:text-plum disabled:opacity-50"
                >
                  <X className="size-3.5" />
                  {cancelandoId === item.id ? "..." : "Trocar"}
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
          disponiveis.map((item) => (
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
                  disabled={item.vagasRestantes <= 0}
                  onClick={() => setItemConfirmar(item)}
                >
                  Presentear
                </Button>
              </div>
            </div>
          ))}
      </div>

      {carregando && (
        <p className="py-6 text-center text-muted-foreground">Carregando presentes...</p>
      )}
      {!carregando && disponiveis.length === 0 && meus.length === 0 && !permiteDinheiro && (
        <p className="py-6 text-center text-muted-foreground">
          Nenhum presente disponível no momento.
        </p>
      )}

      {/* Confirmação de presente */}
      <Modal open={!!itemConfirmar} onClose={() => !reservando && setItemConfirmar(null)}>
        <div className="space-y-4 text-center">
          <div className="brand-gradient mx-auto grid size-12 place-items-center rounded-2xl text-white">
            <Gift className="size-6" />
          </div>
          <div>
            <h3 className="font-serif text-2xl">Confirmar presente</h3>
            <p className="mt-1 text-sm text-muted-foreground">
              Deseja presentear o casal com <strong>{itemConfirmar?.nome}</strong>?
            </p>
          </div>
          <div className="flex gap-2">
            <Button
              variant="outline"
              className="h-11 flex-1"
              onClick={() => setItemConfirmar(null)}
              disabled={reservando}
            >
              Cancelar
            </Button>
            <Button
              className="brand-gradient h-11 flex-1 border-0 text-white hover:opacity-95"
              onClick={reservar}
              disabled={reservando}
            >
              <Check className="size-4" />
              {reservando ? "Confirmando..." : "Sim, presentear"}
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
            <Label htmlFor="valor">Valor (R$)</Label>
            <Input
              id="valor"
              inputMode="decimal"
              className="h-12 text-base"
              placeholder="100,00"
              value={valor}
              onChange={(e) => setValor(e.target.value)}
            />
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
